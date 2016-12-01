import time


class MartiServer:
    """
    :type bandwidth int
    """

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


class ATAKLiteClient:
    """
    :type image_broadcast_interval_ms: int
    :type latestsa_broadcast_interval_ms: int
    :type count: int
    :type present_resources: list[str]
    :type required_properties: list[str]
    """

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
                 image_broadcast_interval_ms,
                 latestsa_broadcast_interval_ms,
                 count,
                 present_resources,
                 required_properties
                 ):
        self.image_broadcast_interval_ms = image_broadcast_interval_ms
        self.latestsa_broadcast_interval_ms = latestsa_broadcast_interval_ms
        self.count = count
        self.present_resources = present_resources
        self.required_properties = required_properties

    def to_dict(self):
        return {
            'imageBroadcastIntervalMS': self.image_broadcast_interval_ms,
            'latestSABroadcastIntervalMS': self.latestsa_broadcast_interval_ms,
            'count': self.count,
            'presentResources': self.present_resources,
            'requiredProperties': self.required_properties
        }


# noinspection PyPep8Naming
class ScenarioConfiguration:
    """
    :type session_identifier: str
    :type server: MartiServer
    :type clients: list[ATAKLiteClient]
    :type minimumRunTimeMS: int
    """

    def __init__(self,
                 session_identifier,
                 server,
                 clients,
                 minimumRunTimeMS,
                 ):
        self.session_identifier = session_identifier
        self.server = server
        self.clients = clients
        self.minimumRunTimeMS = minimumRunTimeMS
        self.server.parent_config = self
        for c in self.clients:
            c.parent_config = self

    def to_dict(self):
        return {
            'sessionIdentifier': self.session_identifier,
            'server': self.server.to_dict(),
            'clients': map(lambda c: ATAKLiteClient.to_dict(c), self.clients),
            'minimumRunTimeMS': self.minimumRunTimeMS,
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
                map(lambda c: ATAKLiteClient.from_dict(c), d['clients']),
                d['minimumRunTimeMS'],
        )
