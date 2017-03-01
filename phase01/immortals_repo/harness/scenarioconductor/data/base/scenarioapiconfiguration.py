import time

from scenarioconductor.data.base.validation import get_properties_list, get_resource_list
from serializable import ValidationCapable


# noinspection PyPep8Naming
class MartiServer(ValidationCapable):
    """
    :type bandwidth int
    """

    _valid_values = {
        'bandwidth': (0, 10000000)
    }

    @classmethod
    def from_dict(cls, d):
        return cls(
            d['bandwidth']
        )

    def __init__(self,
                 bandwidth
                 ):
        self.bandwidth = bandwidth

    def to_dict(self):
        return {
            'bandwidth': self.bandwidth
        }

    def equals(self, other):
        """
        :param MartiServer other:
        """
        return self.bandwidth == other.bandwidth


# noinspection PyPep8Naming
class ATAKLiteClient(ValidationCapable):
    """
    :type imageBroadcastIntervalMS: int
    :type latestSABroadcastIntervalMS: int
    :type count: int
    :type presentResources: list[str]
    :type requiredProperties: list[str]
    """

    _valid_values = {
        'imageBroadcastIntervalMS': (1000, 60000),
        'latestSABroadcastIntervalMS': (1000, 60000),
        'count': (2, 6),
        'presentResources': get_resource_list(),
        'requiredProperties': get_properties_list()
    }

    @classmethod
    def from_dict(cls, d):
        return cls(
            d['imageBroadcastIntervalMS'],
            d['latestSABroadcastIntervalMS'],
            d['count'],
            d['presentResources'],
            d['requiredProperties']
        )

    def __init__(self,
                 imageBroadcastIntervalMS,
                 latestSABroadcastIntervalMS,
                 count,
                 presentResources,
                 requiredProperties
                 ):
        self.imageBroadcastIntervalMS = imageBroadcastIntervalMS
        self.latestSABroadcastIntervalMS = latestSABroadcastIntervalMS
        self.count = count
        self.presentResources = presentResources
        self.requiredProperties = requiredProperties

    def to_dict(self):
        return {
            'imageBroadcastIntervalMS': self.imageBroadcastIntervalMS,
            'latestSABroadcastIntervalMS': self.latestSABroadcastIntervalMS,
            'count': self.count,
            'presentResources': self.presentResources,
            'requiredProperties': self.requiredProperties
        }

    def equals(self, other):
        """
        :param  ATAKLiteClient other:
        """
        isEqual = \
            self.imageBroadcastIntervalMS == other.imageBroadcastIntervalMS \
            and self.latestSABroadcastIntervalMS == other.latestSABroadcastIntervalMS \
            and self.count == other.count

        if not isEqual or len(self.presentResources) != len(other.presentResources) \
                or len(self.requiredProperties) != len(other.requiredProperties):
            return False

        else:
            for res in self.presentResources:
                if res not in other.presentResources:
                    return False

            for prop in self.requiredProperties:
                if prop not in other.requiredProperties:
                    return False

        return True


# noinspection PyPep8Naming
class ScenarioConductorConfiguration(ValidationCapable):
    """
    :type sessionIdentifier: str
    :type server: MartiServer
    :type clients: list[ATAKLiteClient]
    """

    _valid_values = {}

    def __init__(self,
                 sessionIdentifier,
                 server,
                 clients
                 ):
        self.sessionIdentifier = sessionIdentifier
        self.server = server
        self.clients = clients
        self.server.parent_config = self
        for c in self.clients:
            c.parent_config = self

    def to_dict(self):
        return {
            'sessionIdentifier': self.sessionIdentifier,
            'server': self.server.to_dict(),
            'clients': map(lambda c: ATAKLiteClient.to_dict(c), self.clients),
        }

    @classmethod
    def from_dict(cls, d):

        if 'sessionIdentifier' in d:
            si = d['sessionIdentifier']
        else:
            si = "S" + str(int(time.time() * 1000))[:12]

        return cls(
            si,
            MartiServer.from_dict(d['server']),
            map(lambda c: ATAKLiteClient.from_dict(c), d['clients'])
        )

    def equals(self, other):
        """
        :param ScenarioConductorConfiguration other:
        :rtype: bool
        """

        return self.server.equals(other.server) and len(self.clients) == 1 and len(other.clients) == 1 and self.clients[
            0].equals(other.clients[0])

# 
# def test():
#     d = {
#         'server': {
#             'bandwidth': 50
#         },
#         'clients': [
#             {
#                 'imageBroadcastIntervalMS': 2000,
#                 'latestSABroadcastIntervalMS': 1000,
#                 'count': 2,
#                 'presentResources': [],
#                 'requiredProperties': []
#             }
#         ]
#     }
# 
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['server']['bandwidth'] = 0
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['server']['bandwidth'] = -1
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['server']['bandwidth'] = 1000001
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['server']['bandwidth'] = 1000000
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['count'] = 1
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['count'] = -1
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['count'] = 0
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['count'] = 1
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['count'] = 13
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['count'] = 12
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'userInterface',
#         'gpsSatellites'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellites'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellitez'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['presentResources'] = [
#         'gpsSatellitez'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellites'
#     ]
#     d['clients'][0]['requiredProperties'] = [
# 
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['requiredProperties'] = [
#         'trustedLocations'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
# 
#     d['clients'][0]['requiredProperties'] = [
#         'trustedLocations',
#         'turzlec'
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
# 
#     d['clients'][0]['requiredProperties'] = [
#         'turkey',
#         'trustedLocations'
# 
#     ]
#     scc = ScenarioConductorConfiguration.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
