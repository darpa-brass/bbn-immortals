import asyncio
import json
import os
import time
import traceback
from asyncio import Queue
from threading import RLock
from typing import List, Dict
from uuid import uuid4

from buildsystem import vine
from buildsystem.datatypes import ActivityStatus, BuildServiceException, dictify, StatusNotification, \
    StatusHandler, StdoutStatusHandler, DasTestbed
from buildsystem.vine import MutableVineTestbed, MutableVineVm

_lock = RLock()

REMOTE_IMMORTALS_ROOT_REPO_DIR = '/home/ubuntu/immortals'

# DAS_BASE_IMAGE_DATE_FORMAT = 'IMMoRTALS DAS Base [%Y-%m-%d %H:%M:%S]'
# ANDROID_BASE_IMAGE_DATE_FORMAT = 'IMMoRTALS Android 21 Emulator Base [%Y-%m-%d %H:%M:%S]'
ANDROID_BASE_IMAGE_REGEX = \
    r'^IMMoRTALS Android 21 Emulator Base \[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\]$'
DAS_BASE_IMAGE_REGEX = r'^IMMoRTALS DAS Base \[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\]$'
DAS_PREDEPLOY_IMAGE_REGEX = r'^IMMoRTALS DAS Pre-Deploy \[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\]$'

DAS_PREDEPLOY_IMAGE_DATE_FORMAT = 'IMMoRTALS DAS Pre-Deploy [%Y-%m-%d %H:%M:%S]'


class MutableDasTestbed(DasTestbed):
    def __init__(self, status: ActivityStatus, testbed: MutableVineTestbed):
        super().__init__(status, testbed)
        self._lock = RLock()

    def get_das_vm(self) -> MutableVineVm:
        return self.testbed.get_vm(self.testbed.testbed_details.get_original_testbed_name() + 'DAS')

    async def _execute_das_command(self, listener: StatusHandler, cmd: List[str]) -> int:
        return await self.get_das_vm().exec_command(cmd=cmd, listener=listener)

    async def das_dependencies_install(self, listener: StatusHandler, branch: str = None) -> StatusNotification:
        with self._lock:
            await listener.send(ActivityStatus.DAS_DEPS_SETUP_IN_PROGRESS, self.get_testbed_name(),
                                "Setting up DAS dependencies.")

            target_repo_dir = REMOTE_IMMORTALS_ROOT_REPO_DIR + ('/trunk' if branch is None else ('/branches/' + branch))

            return_code = await self._execute_das_command(listener, [
                'python3.5', target_repo_dir + '/shared/utils/install.py',
                '--buildsystem-predeploy-setup'])

            if return_code == 0:
                return StatusNotification(ActivityStatus.DAS_DEPS_SETUP_SUCCESSFUL, self.get_testbed_name(),
                                          "DAS dependencies set up successfully.")
            else:
                raise BuildServiceException(ActivityStatus.DAS_DEPS_SETUP_FAILURE, self.get_testbed_name(),
                                            "DAS dependencies failed to update!")

    async def das_repo_update(self, listener: StatusHandler, branch: str = None) -> StatusNotification:
        with self._lock:
            await listener.send(ActivityStatus.DAS_REPO_UPDATING, self.get_testbed_name(), "Updating DAS Repository")
            subdir = 'trunk' if branch is None else ('branches/' + branch)
            return_code = await self._execute_das_command(listener,
                                                          ['svn', 'update', '/home/ubuntu/immortals/' + subdir])
            if return_code == 0:
                return StatusNotification(ActivityStatus.DAS_REPO_UPDATE_SUCCESSFUL, self.get_testbed_name(),
                                          'DAS source root updated successfully.')
            else:
                raise BuildServiceException(
                    ActivityStatus.DAS_REPO_UPDATE_FAILURE, self.get_testbed_name(),
                    'DAS source root failed to update!')

    async def das_deploy(self, listener: StatusHandler, cp_profile: str = None,
                         branch: str = None) -> StatusNotification:
        with self._lock:
            await listener.send(
                ActivityStatus.DAS_DEPLOY_IN_PROGRESS, self.get_testbed_name(),
                'Starting DAS Deployment' +
                ('' if cp_profile is None else ' with challenge problem configuration ' + cp_profile) + '.')

            target_repo_dir = REMOTE_IMMORTALS_ROOT_REPO_DIR + ('/trunk' if branch is None else ('/branches/' + branch))

            cmd = [
                target_repo_dir + '/gradlew',
            ]

            if cp_profile is not None:
                cmd = cmd + ['--cp-profile', cp_profile]

            cmd = cmd + [
                '--build-file', target_repo_dir + '/build.gradle',
                '--project-dir', target_repo_dir,
                'deploy'
            ]

            return_code = await self._execute_das_command(listener, cmd)
            if return_code == 0:
                return StatusNotification(ActivityStatus.DAS_DEPLOY_SUCCESSFUL, self.get_testbed_name(),
                                          "The DAS was deployed successfully.")
            else:
                raise BuildServiceException(
                    ActivityStatus.DAS_DEPLOY_FAILURE, self.get_testbed_name(),
                    "The DAS Deployment failed!")

    async def das_execute_test(self, listener: StatusHandler, test_identifier: str,
                               cp_profile: str = None, branch: str = None) -> StatusNotification:
        with self._lock:
            await listener.send(
                ActivityStatus.DAS_TESTING_STARTED, self.get_testbed_name(),
                'Starting testing of ' + test_identifier + '...')

            target_repo_dir = REMOTE_IMMORTALS_ROOT_REPO_DIR + ('/trunk' if branch is None else ('/branches/' + branch))

            cmd = [
                os.path.join('bash', target_repo_dir, 'shared', 'tools.sh'),
                '--env-profile', 'vine'
            ]

            if cp_profile is not None:
                cmd = cmd + ['--cp-profile', cp_profile]

            cmd = cmd + ['integrationtest', test_identifier]

            return_code = await self._execute_das_command(listener, cmd)

            if return_code == 0:
                return StatusNotification(ActivityStatus.DAS_TESTING_SUCCESSFUL, self.get_testbed_name(),
                                          "The DAS Tests completed successfully.")
            else:
                raise BuildServiceException(
                    ActivityStatus.DAS_TESTING_FAILURE, self.get_testbed_name(),
                    "The DAS Tests failed!")


class TestbedData:
    def __init__(self):
        self._unused_jenkins_android_testbeds = Queue()
        self._unused_jenkins_plain_testbeds = Queue()

        if os.path.exists('vine_testbed_data.json'):
            data = json.load(open('vine_testbed_data.json', 'r'))

            # Gather all testbed ids and get their data in a single network call
            all_testbed_ids = list()
            all_testbed_ids = all_testbed_ids + list(data['unused_jenkins_android_testbeds'].keys())
            all_testbed_ids = all_testbed_ids + list(data['unused_jenkins_plain_testbeds'].keys())
            all_testbed_ids = all_testbed_ids + list(data['unmanaged_testbeds'].keys())
            all_testbed_ids = all_testbed_ids + list(data['used_jenkins_testbeds'].keys())
            all_vine_testbeds = vine.get_testbeds_by_ids(list(map(lambda x: int(x), all_testbed_ids)))

            def get_das_testbeds(data_label: str) -> List[MutableDasTestbed]:
                rval = list()

                for testbed_id, testbed_data in data[data_label].items():
                    testbed_list = list(filter(lambda x: x.testbed_id == int(testbed_id), all_vine_testbeds))
                    if len(testbed_list) > 0:
                        rval.append(MutableDasTestbed(
                            status=ActivityStatus[testbed_data['status']],
                            testbed=testbed_list[0]))

                return rval

            unused_jenkins_android_testbeds = get_das_testbeds('unused_jenkins_android_testbeds')
            unused_jenkins_plain_testbeds = get_das_testbeds('unused_jenkins_plain_testbeds')
            self._unmanaged_testbeds = get_das_testbeds('unmanaged_testbeds')
            self._used_jenkins_testbeds = get_das_testbeds('used_jenkins_testbeds')

            for val in unused_jenkins_android_testbeds:
                self._unused_jenkins_android_testbeds.put_nowait(val)

            for val in unused_jenkins_plain_testbeds:
                self._unused_jenkins_plain_testbeds.put_nowait(val)

        else:
            self._unmanaged_testbeds = list()
            self._used_jenkins_testbeds = list()

        self._lock = RLock()

    def _save_state(self):
        with self._lock:
            def prep_testbeds(testbeds: List[MutableDasTestbed]) -> Dict[str, Dict]:
                d = dict()
                for tb in testbeds:  # type: MutableDasTestbed
                    d[str(tb.testbed.testbed_id)] = {
                        'status': tb.status.name
                    }
                return d

            data = {
                'unused_jenkins_android_testbeds': prep_testbeds(list(self._unused_jenkins_android_testbeds._queue)),
                'unused_jenkins_plain_testbeds': prep_testbeds(list(self._unused_jenkins_plain_testbeds._queue)),
                'used_jenkins_testbeds': prep_testbeds(self._used_jenkins_testbeds),
                'unmanaged_testbeds': prep_testbeds(self._unmanaged_testbeds)
            }
            data_str = json.dumps(data, indent=4, sort_keys=True)
            open('vine_testbed_data.json', 'w').write(data_str)

    def add_unmanaged_testbed(self, testbed: MutableDasTestbed):
        self._unmanaged_testbeds.append(testbed)
        self._save_state()

    def remove_unmanaged_testbed_by_name(self, testbed_name: str):
        with self._lock:
            old_testbed = \
                list(filter(lambda x: x.testbed.testbed_details.testbed_name == testbed_name,
                            self._unmanaged_testbeds))[
                    0]
            self._unmanaged_testbeds.remove(old_testbed)
            self._save_state()

    async def claim_buildpool_plain_testbed(self, testbed_new_name: str) -> MutableDasTestbed:
        return await self._claim_buildpool_testbed(testbed_new_name=testbed_new_name,
                                                   testbed_queue=self._unused_jenkins_plain_testbeds)

    async def claim_buildpool_android_testbed(self, testbed_new_name: str) -> MutableDasTestbed:
        return await self._claim_buildpool_testbed(testbed_new_name=testbed_new_name,
                                                   testbed_queue=self._unused_jenkins_android_testbeds)

    async def _claim_buildpool_testbed(self, testbed_new_name: str, testbed_queue: Queue) -> MutableDasTestbed:
        with self._lock:
            testbed = await testbed_queue.get()  # type: MutableDasTestbed
            testbed.testbed.rename_testbed(testbed_name=testbed_new_name)
            self._used_jenkins_testbeds.append(testbed)
            self._save_state()
            return testbed

    def add_build_pool_plain_testbed(self, testbed: MutableDasTestbed):
        self._unused_jenkins_plain_testbeds.put_nowait(testbed)
        self._save_state()

    def add_build_pool_android_testbed(self, testbed: MutableDasTestbed):
        self._unused_jenkins_android_testbeds.put_nowait(testbed)
        self._save_state()

    def remove_used_build_tool_testbed(self, old_testbed: MutableDasTestbed):
        with self._lock:
            self._used_jenkins_testbeds.remove(old_testbed)
            self._save_state()

    def remove_used_build_tool_testbed_by_name(self, testbed_name: str):
        with self._lock:
            old_testbed = \
                list(filter(lambda x: x.testbed.testbed_details.testbed_name == testbed_name,
                            self._used_jenkins_testbeds))[
                    0]
            self._used_jenkins_testbeds.remove(old_testbed)
            self._save_state()

    def get_active_testbed(self, testbed_name: str) -> MutableDasTestbed:
        with self._lock:
            try:
                results = list(filter(
                    lambda x: x.testbed.testbed_details.testbed_name == testbed_name,
                    self._used_jenkins_testbeds))

                if len(results) == 0:
                    results = list(filter(
                        lambda x: x.testbed.testbed_details.testbed_name == testbed_name,
                        self._unmanaged_testbeds))

                return results[0]
            except Exception as e:
                print(str(e))
                traceback.print_exc()
                raise BuildServiceException(ActivityStatus.SERVER_ERROR, testbed_name,
                                            'Unable to get the testbed! Are you sure it exists?')


async def _construct_immortals_testbed(listener: StatusHandler, testbed_name: str,
                                       testbed_desc: str, include_android: bool) -> StatusNotification:
    with _lock:
        status = ActivityStatus.TESTBED_INITIALIZING
        try:
            latest_images = vine._get_latest_matching_images([DAS_PREDEPLOY_IMAGE_REGEX, ANDROID_BASE_IMAGE_REGEX])

            await listener.send(status, testbed_name, 'Creating empty testbed...')
            testbed = vine.create_testbed(testbed_name=testbed_name, testbed_desc=testbed_desc)
            await asyncio.sleep(10)
            await listener.send(status, testbed_name, 'Empty testbed created.')
            # testbed = vine.get_testbed(testbed_name)

            await listener.send(status, testbed_name, 'Starting DAS...')
            das_vm = await testbed.add_vm(vm_name=testbed_name + 'DAS',
                                          image_id=latest_images[DAS_PREDEPLOY_IMAGE_REGEX].image_id,
                                          flavor_id=3)
            await asyncio.sleep(10)
            await listener.send(status, testbed_name, 'DAS started.')

            if include_android:
                await listener.send(status, testbed_name, 'Starting Android Emulator 00...')
                emu0_vm = await testbed.add_vm(vm_name=testbed_name + 'Emulator00',
                                               image_id=latest_images[ANDROID_BASE_IMAGE_REGEX].image_id,
                                               flavor_id=16)
                await asyncio.sleep(10)
                await listener.send(status, testbed_name, 'Android Emulator 00 started.')

                await listener.send(status, testbed_name, 'Starting Android Emulator 01...')
                emu1_vm = await testbed.add_vm(vm_name=testbed_name + 'Emulator01',
                                               image_id=latest_images[ANDROID_BASE_IMAGE_REGEX].image_id,
                                               flavor_id=16)
                await listener.send(status, testbed_name, 'Android Emulator 01 started.')

            wait_time = 30
            await listener.send(status, testbed_name, "Waiting " + str(wait_time) + " seconds for things to settle...")
            await asyncio.sleep(wait_time)

            immortals_override_dict = {
                "deploymentEnvironment": {
                    "martiAddress": das_vm.vm_details.public_ip,
                }
            }

            if include_android:
                immortals_override_dict["deploymentEnvironment"]["androidEnvironments"] = [
                    {
                        "adbIdentifier": emu0_vm.vm_details.public_ip + ":5432",
                        "adbPort": 5432,
                        "adbUrl": emu0_vm.vm_details.public_ip,
                        "environmentDetails": {
                            "androidVersion": 21,
                            "externallyAccessibleUrls": [
                                "dropbox.com:443",
                                "dropbox.com:80"
                            ],
                            "uploadBandwidthLimitKilobitsPerSecond": 800
                        }
                    },
                    {
                        "adbIdentifier": emu1_vm.vm_details.public_ip + ":5432",
                        "adbPort": 5432,
                        "adbUrl": emu1_vm.vm_details.public_ip,
                        "environmentDetails": {
                            "androidVersion": 21
                        }
                    }
                ]

            tmp_filepath = str(uuid4()) + '-tmp_immortals_override.json'

            open(tmp_filepath, 'w').write(json.dumps(immortals_override_dict, indent=4, sort_keys=True))
            await das_vm.copy_file(tmp_filepath, '~/immortals_override_file.json')
            await das_vm.exec_command(
                ['\'echo "export IMMORTALS_OVERRIDE_FILE=~/immortals_override_file.json" >> ${HOME}/.bashrc\''])

            await das_vm.exec_command(
                ['\'echo "export IMMORTALS_ENV_PROFILE=vine" >> ${HOME}/.bashrc\''])

            os.remove(tmp_filepath)

            testbed._update_vms_and_gateways()

            dtb = MutableDasTestbed(ActivityStatus.TESTBED_READY, testbed=testbed)

            return StatusNotification(status=dtb.status, testbed_name=testbed_name,
                                      message="Testbed ready for use.", data=dictify(dtb, MutableDasTestbed))

        except Exception as e:
            traceback.print_exc()
            msg = str(e) + "\n" + traceback.format_exc()
            raise BuildServiceException(ActivityStatus.SERVER_ERROR, testbed_name, msg)


_persistent_testbed_data = TestbedData()


async def construct_unmanaged_testbed(listener: StatusHandler, testbed_name: str,
                                      include_android: bool) -> StatusNotification:
    with _lock:
        notification = await _construct_immortals_testbed(listener=listener, testbed_name=testbed_name,
                                                          testbed_desc="Unmanaged IMMoRTALS Testbed",
                                                          include_android=include_android)
        testbed = MutableDasTestbed(status=ActivityStatus.TESTBED_READY,
                                    testbed=vine.get_testbed(testbed_name=testbed_name))
        _persistent_testbed_data.add_unmanaged_testbed(testbed)

    return notification


async def delete_testbed(listener: StatusHandler, testbed_name: str) -> StatusNotification:
    with _lock:
        notification = await vine.delete_testbed(listener=listener, testbed_name=testbed_name)
        _persistent_testbed_data.remove_unmanaged_testbed_by_name(testbed_name=testbed_name)
        return notification


async def claim_buildsystem_plain_testbed(listener: StatusHandler, testbed_name: str) -> StatusNotification:
    await listener.send(ActivityStatus.TESTBED_INITIALIZING, testbed_name=testbed_name,
                        message="Waiting for testbed from pool to become available...")
    await _persistent_testbed_data.claim_buildpool_plain_testbed(testbed_new_name=testbed_name)
    return StatusNotification(ActivityStatus.TESTBED_READY, testbed_name=testbed_name,
                              message="Testbed claimed.")


async def claim_buildsystem_android_testbed(listener: StatusHandler, testbed_name: str) -> StatusNotification:
    await listener.send(ActivityStatus.TESTBED_INITIALIZING, testbed_name=testbed_name,
                        message="Waiting for testbed from pool to become available...")
    await _persistent_testbed_data.claim_buildpool_android_testbed(testbed_new_name=testbed_name)
    return StatusNotification(ActivityStatus.TESTBED_READY, testbed_name=testbed_name,
                              message="Testbed claimed.")


async def add_buildsystem_testbed_to_pool(listener: StatusHandler, include_android: bool) -> StatusNotification:
    with _lock:
        testbed_name = 'jenkins' + str(int(time.time() * 1000))
        await _construct_immortals_testbed(listener=listener,
                                           testbed_name=testbed_name,
                                           testbed_desc="Managed Jenkins Testbed", include_android=include_android)
        vine_testbed = vine.get_testbed(testbed_name=testbed_name)
        das_testbed = MutableDasTestbed(status=ActivityStatus.TESTBED_READY, testbed=vine_testbed)
        _persistent_testbed_data.add_build_pool_plain_testbed(das_testbed)
        return StatusNotification(status=ActivityStatus.TESTBED_READY, testbed_name=testbed_name,
                                  message="A new testbed with" + ("" if include_android else "out") +
                                          " Android emulators has been added to the build pool.",
                                  data=None)


async def destroy_and_trigger_testbed_replacement(listener: StatusHandler, testbed_name: str,
                                                  validate: bool = True) -> StatusNotification:
    if not validate:
        listener = StdoutStatusHandler()

    async def _destroy_and_trigger_testbed_replacement():
        with _lock:
            notification = await vine.delete_testbed(listener=listener, testbed_name=testbed_name)
            await listener.send_notification(notification)
            is_android = _persistent_testbed_data.get_active_testbed(testbed_name=testbed_name).has_android_vms()
            _persistent_testbed_data.remove_used_build_tool_testbed_by_name(testbed_name=testbed_name)
            notification = await add_buildsystem_testbed_to_pool(listener=listener, include_android=is_android)
            return notification

    if validate:
        return await _destroy_and_trigger_testbed_replacement()
    else:
        asyncio.ensure_future(_destroy_and_trigger_testbed_replacement())
        return StatusNotification(status=ActivityStatus.TESTBED_IGNORE_RESULT, testbed_name=testbed_name,
                                  message='Deleting testbed "' + testbed_name + '" and replacing with a clean one.')


def get_testbed(testbed_name: str) -> MutableDasTestbed:
    return _persistent_testbed_data.get_active_testbed(testbed_name=testbed_name)

# async def rebuild_predeploy_das_image(self, listener: StatusHandler, testbed_name: str) -> StatusNotification:
#     # TODO: Better cleanup on failure
#     # TODO: Add locking
#
#     latest_images = _get_latest_matching_images([DAS_BASE_IMAGE_REGEX, ANDROID_BASE_IMAGE_REGEX])
#     das_image = latest_images[DAS_BASE_IMAGE_REGEX]
#     android_image = latest_images[ANDROID_BASE_IMAGE_REGEX]
#
#     # testbed_configuration = _vine_upload_testbed_configuration(das_image=das_image, android_image=android_image)
#
#     status_notification = await self._spawn_testbed(
#         listener=listener,
#         testbed_configuration_remote_filepath=testbed_configuration.remote_configuration_filepath,
#         testbed_name=testbed_name)
#     await listener.send_notification(status_notification)
#
#     testbed = await self.get_existing_testbed(listener, testbed_name)  # type: TestBed
#
#     await asyncio.sleep(20)
#
#     status_notification = await testbed.das_repo_update(listener=listener)
#     await listener.send_notification(status_notification)
#
#     status_notification = await testbed.das_dependencies_install(listener=listener)
#     await listener.send_notification(status_notification)
#
#     status_notification = await self.halt_testbed(listener=listener, testbed_name=testbed_name)
#     await listener.send_notification(status_notification)
#
#     status_notification = await self.snapshot_das_vm(listener, testbed_name,
#                                                      datetime.now().strftime(DAS_PREDEPLOY_IMAGE_DATE_FORMAT))
#     return status_notification
#
# def _testbed_exists_on_vine(testbed_name: str):
#     data, timestamp = _vine_net_exec('vineservice.list.testbeds', {})
#
#     for tb in data['testbeds']:
#         tb_data = data['testbeds'][tb]
#         if tb_data['testbed_group'] == VINE_GROUP and tb_data['testbed_name'] == testbed_name:
#             return True
#     return False
#
#
# class BuildManager:
#     def __init__(self):
#         self._testbeds = dict()  # type: Dict[str, DasTestbed]
#         self._testbed_lock = RLock()  # type: RLock
#
#     async def _status_handler(self, testbed_name: str, currently_initializing_task=None,
#                               currently_ready_task=None,
#                               currently_testing_started_task=None,
#                               currently_testing_finished_task=None, currently_deleting_task=None,
#                               currently_no_existing_testbed_task=None) -> StatusNotification:
#
#         with self._testbed_lock:
#             if testbed_name in self._testbeds:
#                 tb = self._testbeds[testbed_name]
#
#                 if tb.status == ActivityStatus.TESTBED_INITIALIZING:
#                     if currently_initializing_task is None:
#                         raise BuildServiceException(
#                             ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                             'The testbed is already initializing!')
#
#                     else:
#                         return await currently_initializing_task()
#
#                 elif tb.status == ActivityStatus.TESTBED_READY:
#                     if currently_ready_task is None:
#                         raise BuildServiceException(
#                             ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                             'The testbed is already set up!!')
#
#                     else:
#                         return await currently_ready_task()
#
#                 elif tb.status == ActivityStatus.DAS_TESTING_STARTED:
#                     if currently_testing_started_task is None:
#                         raise BuildServiceException(
#                             ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                             'The testbed is currently running a test!')
#
#                     else:
#                         return await currently_testing_started_task()
#
#                 elif tb.status == ActivityStatus.DAS_TESTING_SUCCESSFUL or \
#                         tb.status == ActivityStatus.DAS_TESTING_FAILURE:
#                     if currently_testing_finished_task is None:
#                         raise BuildServiceException(
#                             ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                             'The testbed has finished being used for a test!')
#
#                     else:
#                         return await currently_testing_finished_task()
#
#                 elif tb.status == ActivityStatus.TESTBED_DELETING:
#                     if currently_deleting_task is None:
#                         raise BuildServiceException(
#                             ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                             'The testbed is in the process of being deleted!!')
#
#                     else:
#                         return await currently_deleting_task()
#
#                 else:
#                     raise BuildServiceException(
#                         ActivityStatus.SERVER_ERROR, testbed_name,
#                         'Unexpected status value of "' + tb.status.value + '"!')
#
#             else:
#                 if _testbed_exists_on_vine(testbed_name):
#                     raise BuildServiceException(
#                         ActivityStatus.SERVER_VINE_STATE_DISCREPANCY, testbed_name,
#                         'Existing testbed exists on vine that is not managed by this gateway!')
#
#                 elif currently_no_existing_testbed_task is None:
#                     raise BuildServiceException(
#                         ActivityStatus.SUBMISSION_ERROR, testbed_name,
#                         "Testbed Not found!")
#
#                 else:
#                     # return currently_no_existing_testbed_task(**parent_params)
#                     return await currently_no_existing_testbed_task()
