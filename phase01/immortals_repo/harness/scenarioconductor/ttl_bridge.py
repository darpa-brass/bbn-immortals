import os

from . import immortalsglobals as ig
from . import threadprocessrouter as tpr


def execute_ttl_generation(scenario_configuration, output_file=None):
    client = scenario_configuration.clients[0]
    res = client.presentResources

    params = [
        'python3',
        './py/mission_perturb.py',
        '--session',
        scenario_configuration.sessionIdentifier,
        '--output',
        'deployment_model.ttl',
        '--template',
        './template/gme-template.ttl',
        '--pli-client-msg-rate',
        str(60000 / int(client.latestSABroadcastIntervalMS)),
        '--image-client-msg-rate',
        str(60000 / int(client.imageBroadcastIntervalMS)),
        '--server-bandwidth',
        str(scenario_configuration.server.bandwidth),
        '--client-device-count',
        str(client.count),
        '--android-bluetooth-resource',
        'yes' if 'bluetooth' in res else 'no',
        '--android-usb-resource',
        'yes' if 'usb' in res else 'no',
        '--android-internal-gps-resource',
        'yes' if 'internalGps' in res else 'no',
        '--android-ui-resource',
        'yes' if 'userInterface' in res else 'no',
        '--gps-satellite-resource',
        'yes' if 'gpsSatellites' in res else 'no',
        '--mission-trusted-comms',
        'yes' if 'trustedLocations' in client.requiredProperties else 'no'
    ]

    if output_file is not None:
        params.append('--output')
        params.append(os.path.abspath(output_file))

    tpr.check_output(args=params, cwd=ig.IMMORTALS_ROOT + 'models/scenario')
