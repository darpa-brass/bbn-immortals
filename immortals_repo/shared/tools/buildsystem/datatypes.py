import inspect
import json
from enum import Enum
from typing import Dict, List, Union

_python_primitives = [Dict, List, str, int, float, bool, None]


def _native_ify(value, clazz, has_default: bool, default_value, ify_method, clazz_processing_method, value_path: str):
    """
    A lot of logic is shared between converting between objects and dicts. So braking out here
    :param value: The value to be converted
    :param clazz: Tye type of the value
    :param ify_method: The method to call for calls to 'this' method (which uses the specific clazz_processing_method)
    :param clazz_processing_method: The method to use when unexpected types are encountered (specialized class types)
    :param value_path: Displays the json path to this object. It makes debugging far far easier.
    """

    if value is None:
        if has_default:
            if default_value is None:
                return None
            else:
                value = default_value
        else:
            assert False, 'Value cannot be None for ' + value_path + '!'

    # TODO: Might be a good idea to use Nullable annotations to indicate if this is valid
    if value is None:
        return value

    if clazz in _python_primitives:
        assert isinstance(value, clazz), \
            'Type missmatch while inserting value "' \
            + value + '" into "' + value_path + " of type '" + clazz.__name__ + "'!"
        return value

    elif clazz.__base__ == Enum:
        if isinstance(value, str):
            return clazz[value]
        elif isinstance(value, clazz):
            return value.name
        else:
            assert False, \
                'Type missmatch while inserting value "' \
                + value + '" into "' + value_path + " of type '" + clazz.__name__ + "'!"

    elif clazz.__base__ == Dict:
        assert isinstance(value, Dict), \
            'Type missmatch while inserting value "' \
            + value + '" into "' + value_path + " of type '" + clazz.__name__ + "'!"
        return_dict = dict()

        dict_key_type = clazz.__args__[0]
        assert dict_key_type in _python_primitives, \
            'Invalid dictionary key type "' + dict_key_type + '" for "' + value_path + "'!"

        dict_value_type = clazz.__args__[1]

        for dict_key in value.keys():
            assert isinstance(dict_key, dict_key_type), \
                'Type missmatch while inserting value "' \
                + dict_key + '" into "' + value_path + " of type '" + dict_key_type.__name__ + "'!"

            dict_value = value[dict_key]
            return_dict[dict_key] = ify_method(dict_value, dict_value_type, False, None, value_path=value_path)

        return return_dict

    elif clazz.__base__ == List:
        assert isinstance(value, clazz), \
            'Type missmatch while inserting value "' + value + '" into "' + value_path + "'!"
        return_list = list()

        list_value_type = clazz.__args__[0]
        for list_value in value:
            return_list.append(ify_method(list_value, list_value_type, False, None, value_path=value_path))

        return return_list

    else:
        return clazz_processing_method(value, clazz, value_path)


def _dictify(value, clazz, has_default: bool, default_value, value_path: str):
    def _clazz_processing_method(_value, _clazz, parent_value_path: str):
        fas = inspect.getfullargspec(_clazz)
        annotations = fas.annotations
        value_dict = dict()

        if fas.defaults is None:
            default_list = list()
        else:
            default_list = list(fas.defaults)

        for param_idx in reversed(range(1, len(fas.args))):
            arg_name = fas.args[param_idx]
            assert arg_name in annotations, 'Unannotated parameter found: "' + value_path + '.' + arg_name + '"!'
            arg_type = annotations[arg_name]
            param_has_default = len(default_list) > 0
            param_default_value = default_list.pop() if param_has_default else None

            assert arg_name in value.__dict__, 'Could not find a property named "' + arg_name + '" in "' + \
                                               value.__class__.__name__ + '"!'
            arg_value = _value.__dict__[arg_name]
            value_dict[arg_name] = _dictify(arg_value, arg_type, has_default=param_has_default,
                                            default_value=param_default_value,
                                            value_path=parent_value_path + '.' + arg_name)

        return value_dict

    return _native_ify(value=value, clazz=clazz, has_default=has_default, default_value=default_value,
                       ify_method=_dictify, clazz_processing_method=_clazz_processing_method,
                       value_path=value_path)


def dictify(value, clazz):
    return _dictify(value=value, clazz=clazz, has_default=False, default_value=None, value_path=clazz.__name__)


def _objectify(value, clazz, has_default: bool, default_value, value_path: str):
    def _clazz_processing_method(_value, _clazz, parent_value_path: str):
        fas = inspect.getfullargspec(_clazz)
        annotations = fas.annotations
        object_dict = dict()

        if fas.defaults is None:
            default_list = list()
        else:
            default_list = list(fas.defaults)

        for param_idx in reversed(range(1, len(fas.args))):
            arg_name = fas.args[param_idx]
            assert arg_name in annotations, 'Unannotated parameter found: "' + parent_value_path + '.' + arg_name + '"!'
            arg_type = annotations[arg_name]
            param_has_default = len(default_list) > 0
            param_default_value = default_list.pop() if param_has_default else None

            if arg_name in _value:
                arg_value = _value[arg_name]

                object_dict[arg_name] = _objectify(arg_value, arg_type, has_default=param_has_default,
                                                   default_value=param_default_value,
                                                   value_path=parent_value_path + '.' + arg_name)
            else:
                # noinspection PyTypeHints
                assert param_has_default and param_default_value is None, \
                    'Value cannot be None for ' + value_path + '.' + arg_name + '!'

        return clazz(**object_dict)

    return _native_ify(value=value, clazz=clazz, ify_method=_objectify, has_default=has_default,
                       default_value=default_value, clazz_processing_method=_clazz_processing_method,
                       value_path=value_path)


def objectify(value: Union[Dict, str], clazz):
    if isinstance(value, str):
        value = json.loads(value)
    return _objectify(value=value, clazz=clazz, has_default=False, default_value=None, value_path=clazz.__name__)


class ActivityStatus(Enum):
    # Good and transient statuses
    TESTBED_INITIALIZING = ('TESTBED_INITIALIZING', None)
    TESTBED_READY = ('TESTBED_READY', 0)

    DAS_REPO_UPDATING = ('DAS_REPO_UPDATING', None)
    DAS_REPO_UPDATE_SUCCESSFUL = ('DAS_REPO_UPDATE_SUCCESSFUL', 0)
    DAS_DEPS_SETUP_IN_PROGRESS = ('DAS_DEPS_SETUP_IN_PROGRESS', None)
    DAS_DEPS_SETUP_SUCCESSFUL = ('DAS_ENV_SETUP_SUCCESSFUL', 0)
    DAS_DEPLOY_IN_PROGRESS = ('DAS_DEPLOY_IN_PROGRESS', None)
    DAS_DEPLOY_SUCCESSFUL = ('DAS_DEPLOY_SUCCESSFUL', 0)
    DAS_TESTING_STARTED = ('DAS_TESTING_STARTED', None)
    DAS_TESTING_SUCCESSFUL = ('DAS_TESTING_SUCCESSFUL', 0)

    TESTBED_HALTING = ('TESTBED_HALTING', None)
    TESTBED_HALTED = ('TESTBED_HALTED', 0)
    TESTBED_DAS_SNAPSHOT_IN_PROGRESS = ('TESTBED_DAS_SNAPSHOT_IN_PROGRESS', None)
    TESTBED_DAS_SNAPSHOT_COMPLETED = ('TESTBED_DAS_SNAPSHOT_COMPLETED', 0)
    TESTBED_DELETING = ('TESTBED_DELETING', None)
    TESTBED_DELETED = ('TESTBED_DELETED', 0)
    TESTBED_IGNORE_RESULT = ('TESTBED_IGNORE_RESULT', 0)

    # Framework errors
    SUBMISSION_ERROR = ('SUBMISSION_ERROR', 200)
    SERVER_ERROR = ('SERVER_ERROR', 201)
    SERVER_VINE_STATE_DISCREPANCY = ('SERVER_VINE_STATE_ERROR', 202)

    # Das setup errors
    DAS_DEPS_SETUP_FAILURE = ('DAS_ENV_SETUP_FAILURE', 211)
    DAS_REPO_UPDATE_FAILURE = ('DAS_REPO_UPDATE_FAILURE', 212)
    DAS_DEPLOY_FAILURE = ('DAS_DEPLOY_FAILURE', 213)

    # Das execution errors
    DAS_TESTING_FAILURE = ('DAS_TEST_FAILURE', 220)

    def __init__(self, identifier, error_code):
        self.identifier = identifier
        self.error_code = error_code


class StatusNotification:
    def __init__(self, status: ActivityStatus, testbed_name: str, message: str, data: Dict = None):
        self.status = status
        self.testbed_name = testbed_name
        self.message = message
        self.data = data

    # def to_dict(self):
    #     return {
    #         'status': self.status.identifier,
    #         'testbed_name': self.testbed_name,
    #         'message': self.message,
    #         'data': self.data
    #     }

    # @classmethod
    # def from_dict(cls, d: Dict):
    #     return cls(
    #         status=ActivityStatus[d['status']],
    #         testbed_name=d['testbed_name'],
    #         message=d['message'],
    #         data=d['data']
    #     )


class BuildServiceException(Exception, StatusNotification):
    def __init__(self, status: ActivityStatus, testbed_name: str, message: str, data: Dict = None):
        super(BuildServiceException, self).__init__(message)
        self.status = status
        self.testbed_name = testbed_name
        self.message = message
        self.data = data


class StatusHandler:
    async def send_notification(self, status_notification: StatusNotification):
        raise NotImplementedError

    async def send(self, status: ActivityStatus, testbed_name: str, message: str, data: Dict = None):
        await self.send_notification(
            status_notification=StatusNotification(status=status, testbed_name=testbed_name, message=message, data=data)
        )

    async def stdout(self, msg: str):
        raise NotImplementedError

    async def send_notification_and_close(self, status_notification: StatusNotification):
        raise NotImplementedError

    async def send_and_close(self, status: ActivityStatus, testbed_name: str, message: str, data: Dict = None):
        await self.send_notification_and_close(
            status_notification=StatusNotification(status=status, testbed_name=testbed_name, message=message, data=data)
        )


class StdoutStatusHandler(StatusHandler):

    async def send_notification(self, status_notification: StatusNotification):
        print('############ COMMAND DATA RECEIVED: ' + json.dumps(
            dictify(status_notification, StatusNotification)) + ' ############')

    async def stdout(self, msg: str):
        print(msg)

    async def send_notification_and_close(self, status_notification: StatusNotification):
        print('############ COMMAND DATA RECEIVED: ' + status_notification.message + ' ############')


class VineTaskStatus:
    def __init__(self, name: str, message: str, percent_complete: int, errors: List[str],
                 successes: List[str], failed: bool, status: int, properties: Dict[str, str],
                 complete_date: str = None):
        self.name = name
        self.message = message
        self.percent_complete = percent_complete
        self.complete_date = complete_date
        self.errors = errors
        self.successes = successes
        self.failed = failed
        self.status = status
        self.properties = properties


class VineTask:
    def __init__(self, task_name: str, task_message: str, task_id: str, status: int):
        self.task_name = task_name
        self.task_message = task_message
        self.task_id = task_id
        self.status = status


class VineTestbedDetails:
    def __init__(self, testbed_group: str, testbed_desc: str, vm_count: int, testbed_modified: int, testbed_name: str,
                 testbed_created: int, provider_props: str = None):
        self.testbed_group = testbed_group
        self.testbed_desc = testbed_desc
        self.vm_count = vm_count
        self.testbed_modified = testbed_modified
        self.testbed_name = testbed_name
        self.testbed_created = testbed_created
        self.provider_props = provider_props

    def get_original_testbed_name(self) -> str:
        if self.provider_props is not None:
            d = json.loads(self.provider_props)
            if 'testbed_name' in d:
                return d['testbed_name']

        return self.testbed_name


class VineImage:
    def __init__(self, image_name: str, image_id: str, image_size: int, password: str, user_uploaded: bool, uuid: str,
                 vm_count: int):
        self.image_name = image_name
        self.image_id = image_id
        self.image_size = image_size
        self.password = password
        self.user_uploaded = user_uploaded
        self.uuid = uuid
        self.vm_count = vm_count


class VineserviceGatewayDetails:
    def __init__(self, netmask: int, net_address: str, used_hosts: int, name: str, max_hosts: int, bcast_address: str,
                 gateway: str, uuid: str = None):
        self.netmask = netmask
        self.net_address = net_address
        self.used_hosts = used_hosts
        self.name = name
        self.max_hosts = max_hosts
        self.bcast_address = bcast_address
        self.gateway = gateway
        self.uuid = uuid


class VineVmDetails:
    def __init__(self, roles: List, vm_accessed: int, tags: List, flavor_id: int, vm_uuid: str, vm_modified: int,
                 ni_obj: Dict, vm_created: int, image_id: int, vm_name: str, events: List, properties: Dict,
                 status: str, public_ip: str = None):
        self.roles = roles
        self.vm_accessed = vm_accessed
        self.tags = tags
        self.flavor_id = flavor_id
        self.vm_uuid = vm_uuid
        self.vm_modified = vm_modified
        self.ni_obj = ni_obj
        self.vm_created = vm_created
        self.image_id = image_id
        self.vm_name = vm_name
        self.events = events
        self.properties = properties
        self.status = status
        self.public_ip = public_ip


class VineserviceEvent:
    def __init__(self, date: int, level: int, source_type: str, id: int, source_id: int, message: str):
        self.date = date
        self.level = level
        self.source_type = source_type
        self.id = id
        self.source_id = source_id
        self.message = message


class VineservicePingResponse:
    def __init__(self, message: str, driver_available: bool, status: int):
        self.message = message
        self.driver_available = driver_available
        self.status = status


class VineVm:
    def __init__(self, vm_id: int, testbed_id: int, vm_details: VineVmDetails):
        self.vm_id = vm_id
        self.testbed_id = testbed_id
        self.vm_details = vm_details


class VineGateway:
    def __init__(self, gateway_id: int, testbed_id: int, gateway_details: VineserviceGatewayDetails):
        self.gateway_id = gateway_id
        self.testbed_id = testbed_id
        self.gateway_details = gateway_details


class VineTestbed:
    def __init__(self, testbed_id: int, testbed_details: VineTestbedDetails, vms: Dict[str, VineVm],
                 gateways: Dict[str, VineGateway]):
        self.testbed_id = testbed_id
        self.testbed_details = testbed_details
        self.vms = vms
        self.gateways = gateways

    def has_vm(self, vm_name: str) -> bool:
        return vm_name in self.vms.keys()

    def get_vm(self, vm_name: str) -> VineVm:
        return self.vms[vm_name]

    def get_gateway(self, gateway_name: str):
        return self.gateways[gateway_name]


class DasTestbed:
    NOCHANGE_CONSTANT = '59edf772-a7b2-11e8-9c74-4f8a829bd399'

    def __init__(self, status: ActivityStatus, testbed: VineTestbed):
        self.status = status
        self.testbed = testbed

    def get_testbed_name(self) -> str:
        return self.testbed.testbed_details.testbed_name

    def has_android_vms(self) -> bool:
        return self.testbed.has_vm(self.testbed.testbed_details.get_original_testbed_name() + 'Emulator00')

    def get_das_vm(self) -> VineVm:
        return self.testbed.get_vm(self.testbed.testbed_details.get_original_testbed_name() + 'DAS')

    def get_android0_vm(self) -> VineVm:
        return self.testbed.get_vm(self.testbed.testbed_details.get_original_testbed_name() + 'Emulator00')

    def get_android1_vm(self) -> VineVm:
        return self.testbed.get_vm(self.testbed.testbed_details.get_original_testbed_name() + 'Emulator01')
