import asyncio
import json
import logging
import os
import re
import socket
import subprocess
import time
import traceback
from threading import RLock
from typing import List, Dict, Tuple

import requests

from buildsystem.datatypes import dictify, objectify, StatusHandler, ActivityStatus, BuildServiceException, \
    StatusNotification, VineTask, VineTaskStatus, VineImage, VineservicePingResponse, VineserviceGatewayDetails, \
    VineserviceEvent, VineVmDetails, VineTestbedDetails, VineVm, VineGateway, VineTestbed

SSH_USERNAME = 'ubuntu'
SSH_PRIVATE_KEY_LOCATION = os.path.join(os.environ['HOME'], '.ssh', 'vine_id_rsa')

VINE_ROOT_URL = 'http://128.89.136.104:8080/vine'

VINE_UPLOAD_URL = VINE_ROOT_URL + '/VineUIExportUploader'
VINE_DOWNLOAD_URL = VINE_ROOT_URL + '/VineUIExportDownloader'
VINE_GROUP = "IMMoRTALS"
LOCAL_VINE_DEPLOYMENT_FILE = 'IMMoRTALSMODELTESTBED-2018-08-27_14-37-12.json'
# LOCAL_BASE_TESTBED_CONFIGURATION_FILE = 'base_testbed_configuration_file.json'
LOCAL_TMP_REBUILD_CONFIGURATION_FILE = 'local_tmp_rebuild_configuration_file.json'
# REMOTE_TMP_REBUILD_CONFIGURATION_FILE = None
MAX_TESTBED_SPAWN_ATTEMPTS = 3

# VINE_MODEL_TESTBED_ID = 77
VINE_NET_URL = VINE_ROOT_URL + '/VineUIRequestDispatcher'

VINE_DATE_FORMAT = '%a, %d %b %Y %H:%M:%S %Z'

_vm_add_attempts = 4


def _vine_net_exec(method: str, params: Dict = None) -> Tuple[Dict, int]:
    logging.debug("SENDING TO VINE: \n\t" + json.dumps({
        'method': method,
        'param': params
    }, indent=4, sort_keys=True))

    payload = {'vine_json': json.dumps(
        {
            'method': method,
            'param': {} if params is None else params
        })
    }

    req = requests.get(VINE_NET_URL, params=payload)

    if req.status_code > 299:
        raise Exception(str(req.status_code) + ('' if req.text is None else req.text))

    date = int(time.mktime(time.strptime(req.headers['Date'], VINE_DATE_FORMAT)))

    logging.debug('VINE RX: ' + json.dumps(json.loads(req.text), indent=4, sort_keys=True))
    return json.loads(req.text), date


async def _wait_for_task_finish(vine_task: VineTask) -> Tuple[VineTaskStatus, int]:
    task_id = vine_task.task_id

    task_status, timestamp = _vine_task_status(task_id)

    while task_status.complete_date is None:
        await asyncio.sleep(1)
        task_status, timestamp = _vine_task_status(task_id)

    if task_status.failed:
        msg = ('ERROR! message: ' + task_status.message + ', errors: ' + str(task_status.errors))
        print(msg)
        print(json.dumps(dictify(task_status, VineTaskStatus), indent=4, sort_keys=True))
        raise Exception(msg)

    return task_status, timestamp


async def _vine_net_exec_wait_for_task_finish(method: str, params: Dict[str, object] = None) \
        -> Tuple[VineTaskStatus, int]:
    data, timestamp = _vine_net_exec(method=method, params=params)
    task = objectify(data, VineTask)  # type: VineTask
    # task_id = data['task_id']
    return await _wait_for_task_finish(vine_task=task)

    # task_status, timestamp = _vine_task_status(task_id)
    #
    # while task_status.complete_date is None:
    #     await asyncio.sleep(1)
    #     task_status, timestamp = _vine_task_status(task_id)
    #
    # if task_status.failed:
    #     msg = ('ERROR! message: ' + task_status.message + ', errors: ' + str(task_status.errors))
    #     print(msg)
    #     print(json.dumps(dictify(task_status, VineTaskStatus), indent=4))
    #     raise Exception(msg)
    #
    # return task_status, timestamp


def _get_latest_matching_images(regex_match_strings: List[str]) -> Dict[str, VineImage]:
    rval = dict()

    existing_image_data = _vine_net_exec('vineservice.list.images', {'show_passwords': False})[0]['images']

    for regex in regex_match_strings:
        latest_image_dict = None

        for key in existing_image_data:
            image_name = existing_image_data[key]['image_name']  # type: str
            if re.match(regex, image_name) and (
                    latest_image_dict is None or image_name >= latest_image_dict['image_name']):
                latest_image_dict = existing_image_data[key]
                latest_image_dict['image_id'] = key

        if latest_image_dict is None:
            raise Exception("Could not find an image matching regex '" + regex + "'!")

        rval[regex] = objectify(latest_image_dict, VineImage)

    return rval


def ping() -> VineservicePingResponse:
    d, timestamp = _vine_net_exec('vineservice.admin',
                                  {
                                      'action': 'ping'
                                  })
    return objectify(d, VineservicePingResponse)  # type: VineservicePingResponse


def _vine_cmd_exec(cmd: List[str]):
    completed = subprocess.run(cmd, stdout=subprocess.PIPE)
    stdout = completed.stdout
    stderr = completed.stderr

    if completed.returncode != 0:
        msg = 'Failed to execute command "' + ' '.join(cmd) + '". Return code: ' + str(completed.returncode)
        print(msg)
        print('STDOUT: ' + stdout)
        print('STDERR: ' + stderr)
        raise Exception(msg)

    return stdout.decode().strip()


def _vine_task_status(task_id: str) -> Tuple[VineTaskStatus, int]:
    data, timestamp = _vine_net_exec('vineservice.task.status',
                                     {
                                         'task_id': task_id
                                     })

    logging.debug("Task status:\n" + json.dumps(data, indent=4, sort_keys=True))
    return VineTaskStatus(**data), timestamp


def _get_gateway(gateway_name: str, testbed_id: int):
    d, timestamp = _vine_net_exec('vineservice.list.gateways',
                                  {
                                      'testbed_id': testbed_id
                                  })
    gateway_id = list(filter(lambda x: d['gateways'][x]['name'] == gateway_name, d['gateways'].keys()))[0]
    gateway_details = objectify(d['gateways'][gateway_id], VineserviceGatewayDetails)
    return MutableVineGateway(gateway_id=gateway_id, testbed_id=testbed_id, gateway_details=gateway_details)


async def _wait_for_update(testbed_id: int, since: int, source_type: str, source_id: int, finished_message_regex: str,
                           timeout_s: int, failed_message_regex: str = None):
    # TODO: Increment the "since" value for each check

    elapsed = 0
    while elapsed < timeout_s:
        response, timestamp = _vine_net_exec('vineservice.get.updates',
                                             {
                                                 'since': since,
                                                 'testbed_id': testbed_id
                                             })

        for event_dict in response['events']:
            event = objectify(event_dict, VineserviceEvent)  # type: VineserviceEvent
            if event.source_type == source_type and event.source_id == source_id:
                if re.match(finished_message_regex, event.message) is not None:
                    return
                elif failed_message_regex is not None and re.match(failed_message_regex, event.message) is not None:
                    raise Exception('Failed occurred while waiting for update! testbed_id: ' + str(testbed_id) +
                                    ', source_type: ' + source_type + ', source_id: ' + str(source_id) +
                                    ', message_regex: ' + failed_message_regex)
        await asyncio.sleep(1)
        elapsed = elapsed + 1

    raise Exception('Timeout of ' + str(timeout_s) + ' occurred while waiting for update! testbed_id: ' +
                    str(testbed_id) + ', source_type: ' + source_type + ', source_id: ' + str(source_id) +
                    ', message_regex: ' + finished_message_regex)


class MutableVineVm(VineVm):
    def __init__(self, vm_id: int, testbed_id: int, vm_details: VineVmDetails):
        super().__init__(vm_id, testbed_id, vm_details)

    def ping(self):
        _vine_net_exec('vineservice.roles.assign',
                       {
                           'vm_id': str(self.vm_id),
                           'role_ids': []
                       })

    async def vm_start(self):
        await _vine_net_exec_wait_for_task_finish('vineservice.apply.testbed',
                                                  {
                                                      "testbed_id": self.testbed_id,
                                                      "pending_entities": {
                                                          str(self.vm_id): {
                                                              "vm_name": self.vm_details.vm_name,
                                                              "ni_obj": {
                                                                  "pending_add_auto": {},
                                                                  "pending_add_user": {},
                                                                  "pending_delete": {}
                                                              },
                                                              "status": "pending_start"
                                                          }
                                                      }
                                                  })

        self.vm_details = _get_vm(self.testbed_id, self.vm_details.vm_name).vm_details

    async def vm_shutdown(self):
        task_status, timestamp = await _vine_net_exec_wait_for_task_finish(
            'vineservice.apply.testbed',
            {
                "testbed_id": str(self.testbed_id),
                "pending_entities": {
                    str(self.vm_id): {
                        "vm_name": self.vm_details.vm_name,
                        "ni_obj": {
                            "pending_add_auto": {},
                            "pending_add_user": {},
                            "pending_delete": {}
                        },
                        "status": "pending_stop"
                    }
                }
            })
        await _wait_for_update(testbed_id=self.testbed_id, since=timestamp, source_type='vm', source_id=self.vm_id,
                               finished_message_regex='^Machine is reachable at .*$', timeout_s=30)
        self.vm_details = _get_vm(self.testbed_id, self.vm_details.vm_name).vm_details

    async def copy_file(self, local_filepath: str, remote_filepath: str) -> int:
        ssh_cmd = [
            'scp', '-o', 'StrictHostKeyChecking=no', '-i', SSH_PRIVATE_KEY_LOCATION, local_filepath,
            SSH_USERNAME + '@' + self.vm_details.public_ip + ':' + remote_filepath
        ]

        print('EXEC: [' + ' '.join(ssh_cmd) + ']')

        process = await asyncio.create_subprocess_shell(' '.join(ssh_cmd), stdout=asyncio.subprocess.PIPE,
                                                        stderr=asyncio.subprocess.STDOUT)

        return await process.wait()

    async def exec_command(self, cmd: List[str], listener: StatusHandler = None) -> int:
        ssh_cmd = [
            'ssh', '-T', SSH_USERNAME + '@' + self.vm_details.public_ip, '-o', 'StrictHostKeyChecking=no', '-i',
            SSH_PRIVATE_KEY_LOCATION
        ]

        ssh_cmd = ssh_cmd + cmd

        print('EXEC: [' + ' '.join(ssh_cmd) + ']')

        if listener is None:
            process = await asyncio.create_subprocess_shell(' '.join(ssh_cmd))
            return await process.wait()

        else:
            process = await asyncio.create_subprocess_exec(*ssh_cmd, stdout=asyncio.subprocess.PIPE,
                                                           stderr=asyncio.subprocess.STDOUT)

            return_code = None
            # While it is not at the end of the output or the process hasn't returned
            # while not process.stdout.at_eof() or process.returncode is None:
            while not process.stdout.at_eof() and return_code is None:
                line = await process.stdout.readline()
                if line == b'':
                    return_code = await asyncio.shield(asyncio.wait_for(process.wait(), 0.2))
                else:
                    await listener.stdout(line.decode().strip())

            if return_code != 0:
                print('BAD RETURN CODE: ' + str(return_code))

            return return_code


def _get_vm(testbed_id: int, vm_name: str) -> MutableVineVm:
    d, timestamp = _vine_net_exec('vineservice.list.vms',
                                  {
                                      'testbed_id': str(testbed_id)
                                  })
    vm_id = list(filter(lambda x: d['vms'][x]['vm_name'] == vm_name, d['vms'].keys()))[0]
    vm_details = objectify(d['vms'][vm_id], VineVmDetails)

    return MutableVineVm(vm_id=int(vm_id), testbed_id=testbed_id, vm_details=vm_details)


class MutableVineGateway(VineGateway):
    def __init__(self, gateway_id: int, testbed_id: int, gateway_details: VineserviceGatewayDetails):
        super().__init__(gateway_id, testbed_id, gateway_details)

    async def add_vm(self, vm: MutableVineVm, node_number: int):
        ip_address = self.gateway_details.gateway[0:self.gateway_details.gateway.rfind('.') + 1] + str(node_number)

        timestamp = str(int(time.time() * 1000))

        _vine_net_exec('vineservice.get.updates',
                       {
                           'since': timestamp,
                           'testbed_id': self.testbed_id
                       })

        await _vine_net_exec_wait_for_task_finish('vineservice.apply.testbed',
                                                  {"testbed_id": self.testbed_id,
                                                   "pending_entities": {
                                                       vm.vm_id: {
                                                           "vm_name": vm.vm_details.vm_name,
                                                           "ni_obj": {
                                                               "pending_add_auto": {},
                                                               "pending_add_user": {
                                                                   "nic0": {
                                                                       "gateway_id": str(
                                                                           self.gateway_id),
                                                                       "ip_address": ip_address,
                                                                       "status": "pending_add"
                                                                   }
                                                               },
                                                               "pending_delete": {}
                                                           },
                                                           "status": "pending_edit_nw"}
                                                   }
                                                   })

    async def add_vms(self, vms: List[MutableVineVm], node_numbers: List[int]):
        timestamp = str(int(time.time() * 1000))

        _vine_net_exec('vineservice.get.updates',
                       {
                           'since': timestamp,
                           'testbed_id': self.testbed_id
                       })

        pending_entities = {}

        for idx in range(0, len(vms)):
            vm = vms[idx]
            ip_address = self.gateway_details.gateway[0:self.gateway_details.gateway.rfind('.') + 1] + str(
                node_numbers[idx])
            pending_entities[vm.vm_id] = {
                "vm_name": vm.vm_details.vm_name,
                "ni_obj": {
                    "pending_add_auto": {},
                    "pending_add_user": {
                        "nic0": {
                            "gateway_id": str(
                                self.gateway_id),
                            "ip_address": ip_address,
                            "status": "pending_add"
                        }
                    },
                    "pending_delete": {}
                },
                "status": "pending_edit_nw"}

        data, timestamp = _vine_net_exec('vineservice.apply.testbed',
                                         {"testbed_id": self.testbed_id,
                                          "pending_entities": pending_entities
                                          })

        _vine_net_exec('vineservice.edit.gateway',
                       {
                           "net_address": self.gateway_details.net_address,
                           "is_vm": False,
                           "max_hosts": self.gateway_details.max_hosts,
                           "gateway": self.gateway_details.gateway,
                           "used_hosts": self.gateway_details.used_hosts,
                           "bcast_address": self.gateway_details.bcast_address,
                           "netmask": self.gateway_details.netmask,
                           "name": self.gateway_details.name,
                           "uuid": self.gateway_details.uuid,
                           "created": None,
                           "modified": None,
                           "dummy": False,
                           "id": str(self.gateway_id)
                       })

        task = objectify(data, VineTask)  # type: VineTask
        return await _wait_for_task_finish(vine_task=task)


class MutableVineTestbed(VineTestbed):
    def __init__(self, testbed_id: int, testbed_details: VineTestbedDetails, vms: Dict[str, MutableVineVm],
                 gateways: Dict[str, MutableVineGateway]):
        super().__init__(testbed_id, testbed_details, vms, gateways)
        self._update_lock = RLock()

    def _update_vms_and_gateways(self, specific_vm_name: str = None,
                                 specific_gateway_name: str = None):
        with self._update_lock:
            d, timestamp = _vine_net_exec('vineservice.list.vms',
                                          {
                                              'testbed_id': str(self.testbed_id)
                                          })

            for gateway in self.gateways.values():  # type: MutableVineGateway
                if str(gateway.gateway_id) in d['gateways']:
                    gateway.gateway_details = objectify(d['gateways'][str(gateway.gateway_id)],
                                                        VineserviceGatewayDetails)

            for vm in self.vms.values():  # type: MutableVineVm
                if str(vm.testbed_id) in d['vms']:
                    vm.vm_details = objectify(d['vms'][str(vm.vm_id)], VineVmDetails)

            if specific_gateway_name is not None:
                gateway_id = list({k for k in d['gateways'] if d['gateways'][k]['name'] == specific_gateway_name})[0]
                gateway_details = objectify(d['gateways'][gateway_id], VineserviceGatewayDetails)
                gateway = MutableVineGateway(testbed_id=int(self.testbed_id), gateway_id=int(gateway_id),
                                             gateway_details=gateway_details)
                self.gateways[gateway_details.gateway_name] = gateway

            if specific_vm_name is not None:
                vm_id = list({k for k in d['vms'] if d['vms'][k]['vm_name'] == specific_vm_name})[0]
                vm_details = objectify(d['vms'][vm_id], VineVmDetails)
                vm = MutableVineVm(testbed_id=int(self.testbed_id), vm_id=int(vm_id), vm_details=vm_details)
                self.vms[vm_details.vm_name] = vm

    def add_gateway(self, gateway_name: str, gateway_ip: str) -> MutableVineGateway:
        socket.inet_aton(gateway_ip)

        ping()

        _vine_net_exec('vineservice.add.gateway',
                       {
                           'testbed_id': str(self.testbed_id),
                           'gateway': gateway_ip,
                           'netmask': '24',
                           'name': gateway_name,
                           'net_address': gateway_ip,
                           'bcast_address': gateway_ip[0:gateway_ip.rfind('.') + 1] + '255',
                           'max_hosts': 254,
                           'used_hosts': 1
                       })

        self.ping()

        self._update_vms_and_gateways(specific_gateway_name=gateway_name)
        return self.gateways[gateway_name]

    def ping(self):
        _vine_net_exec('vineservice.edit.testbed',
                       {
                           'testbed_id': self.testbed_id,
                           'testbed_name': self.testbed_details.testbed_name,
                           'testbed_group': self.testbed_details.testbed_group,
                           'testbed_desc': self.testbed_details.testbed_desc
                       })

    def has_vm(self, vm_name: str) -> bool:
        self._update_vms_and_gateways(specific_vm_name=vm_name)
        return vm_name in self.vms.keys()

    def get_vm(self, vm_name: str) -> MutableVineVm:
        self._update_vms_and_gateways(specific_vm_name=vm_name)
        return self.vms[vm_name]

    def get_gateway(self, gateway_name: str):
        self._update_vms_and_gateways(specific_gateway_name=gateway_name)
        return self.gateways[gateway_name]

    async def add_vm(self, vm_name: str, image_id: int, flavor_id: int) -> MutableVineVm:
        global _vm_add_attempts
        rval = None
        failed_attempt_counter = 0
        last_exception = None

        while rval is None and failed_attempt_counter < _vm_add_attempts:
            # noinspection PyBroadException
            try:
                d, timestamp = await _vine_net_exec_wait_for_task_finish('vineservice.apply.testbed',
                                                                         {
                                                                             'testbed_id': str(self.testbed_id),
                                                                             'pending_entities': {
                                                                                 vm_name: {
                                                                                     'vm_name': vm_name,
                                                                                     'ni_obj': {
                                                                                         'pending_add_auto': {},
                                                                                         'pending_add_user': {},
                                                                                         'pending_delete': {},
                                                                                     },
                                                                                     'status': 'pending_add',
                                                                                     'image_id': str(image_id),
                                                                                     'flavor_id': flavor_id
                                                                                 }
                                                                             }
                                                                         })

                vm = self.get_vm(vm_name)

                await _wait_for_update(testbed_id=self.testbed_id, since=timestamp, source_type='vm',
                                       source_id=vm.vm_id,
                                       finished_message_regex='^Machine is reachable at .*$', timeout_s=600,
                                       failed_message_regex='^.*machine is in error state$')

                self._update_vms_and_gateways(specific_vm_name=vm_name)
                rval = self.vms[vm_name]

            except Exception as e:
                last_exception = e
                failed_attempt_counter = failed_attempt_counter + 1
                await self.delete_vm(vm_name)
                await asyncio.sleep(20)
                rval = None

        if rval is None:
            raise last_exception
        else:
            return rval

    async def delete_vm(self, vm_name: str):
        vm = _get_vm(testbed_id=self.testbed_id, vm_name=vm_name)
        await _vine_net_exec_wait_for_task_finish('vineservice.apply.testbed',
                                                  {
                                                      'testbed_id': self.testbed_id,
                                                      'pending_entities': {
                                                          vm.vm_id: {
                                                              'vm_name': vm_name,
                                                              'ni_obj': {
                                                                  'pending_add_auto': {},
                                                                  'pending_add_user': {},
                                                                  'pending_delete': {},
                                                              },
                                                              'status': 'pending_delete'
                                                          }
                                                      }
                                                  })

    def rename_testbed(self, testbed_name: str):
        _vine_net_exec('vineservice.edit.testbed',
                       {
                           'testbed_id': self.testbed_id,
                           'testbed_name': testbed_name,
                           'testbed_group': self.testbed_details.testbed_group,
                           'testbed_desc': self.testbed_details.testbed_desc
                       })
        update_testbeds_details([self])


def _get_testbed_by_id(testbed_id: int):
    d, timestamp = _vine_net_exec('vineservice.list.testbeds', None)

    return MutableVineTestbed(testbed_id=testbed_id,
                              testbed_details=objectify(d['testbeds'][str(testbed_id)], VineTestbedDetails),
                              vms=dict(), gateways=dict())


def update_testbeds_details(testbeds: List[MutableVineTestbed]):
    d, timestamp = _vine_net_exec('vineservice.list.testbeds', None)

    for tb in testbeds:
        if str(tb.testbed_id) in d['testbeds']:
            tb.testbed_details = objectify(d['testbeds'][str(tb.testbed_id)], VineTestbedDetails)


def get_vms_by_id(testbed_id: int) -> List[MutableVineVm]:
    rval = list()
    d, timestamp = _vine_net_exec('vineservice.list.vms',
                                  {
                                      'testbed_id': str(testbed_id)
                                  })

    for vm_id in d['vms'].keys():
        vm_details = objectify(d['vms'][str(vm_id)], VineVmDetails)
        rval.append(MutableVineVm(
            vm_id=int(vm_id),
            testbed_id=testbed_id,
            vm_details=vm_details

        ))

    return rval


def create_testbed(testbed_name: str, testbed_desc: str) -> MutableVineTestbed:
    d, timestamp = _vine_net_exec('vineservice.add.testbed',
                                  {
                                      "testbed_name": testbed_name,
                                      "testbed_group": VINE_GROUP,
                                      "testbed_desc": testbed_desc})

    # def __init__(self, testbed_id: int, testbed: Dict[str, VineTestbedDetails], status: int):
    testbed_id = d['testbed_id']
    testbed_details = objectify(d['testbed'][str(testbed_id)], VineTestbedDetails)
    return MutableVineTestbed(testbed_id=testbed_id, testbed_details=testbed_details, vms=dict(), gateways=dict())


async def delete_testbed(listener: StatusHandler, testbed_name: str) -> StatusNotification:
    tb = get_testbed(testbed_name=testbed_name)
    assert tb.testbed_details.testbed_group == VINE_GROUP
    await listener.send(ActivityStatus.TESTBED_DELETING, testbed_name=testbed_name, message="Deleting testbed...")
    await _vine_net_exec_wait_for_task_finish('vineservice.remove.testbed',
                                              {
                                                  'testbed_id': tb.testbed_id
                                              })
    return StatusNotification(ActivityStatus.TESTBED_DELETED, testbed_name, "Testbed deleted.")


def get_testbeds_by_ids(testbed_ids: List[int]) -> List[MutableVineTestbed]:
    rval = list()

    d, timestamp = _vine_net_exec('vineservice.list.testbeds', None)

    for testbed_id in testbed_ids:
        if str(testbed_id) in d['testbeds']:
            testbed_vms = get_vms_by_id(testbed_id=testbed_id)
            testbed_dict = dict()
            for testbed_vm in testbed_vms:
                testbed_dict[testbed_vm.vm_details.vm_name] = testbed_vm

            rval.append(MutableVineTestbed(testbed_id=int(testbed_id),
                                           testbed_details=objectify(d['testbeds'][str(testbed_id)], VineTestbedDetails),
                                           vms=testbed_dict,
                                           gateways=dict()))

        else:
            print('Testbed with id ' + str(testbed_id) + ' no longer exists on the vine cluster.')
    return rval


def get_testbed(testbed_name: str) -> MutableVineTestbed:
    try:
        d, timestamp = _vine_net_exec('vineservice.list.testbeds', None)

        testbed_id = list(filter(lambda x: d['testbeds'][x]['testbed_name'] == testbed_name, d['testbeds'].keys()))[0]
        return MutableVineTestbed(testbed_id=int(testbed_id),
                                  testbed_details=objectify(d['testbeds'][testbed_id], VineTestbedDetails),
                                  vms=dict(), gateways=dict())
    except Exception as e:
        print(str(e))
        traceback.print_exc()
        raise BuildServiceException(ActivityStatus.SERVER_ERROR, testbed_name,
                                    message='Could not get testbed with the name "' + testbed_name + "'!")
