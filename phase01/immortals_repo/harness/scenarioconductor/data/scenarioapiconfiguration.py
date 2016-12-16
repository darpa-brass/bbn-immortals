import time


# noinspection PyPep8Naming
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


# noinspection PyPep8Naming
class ATAKLiteClient:
    """
    :type imageBroadcastIntervalMS: int
    :type latestSABroadcastIntervalMS: int
    :type count: int
    :type presentResources: list[str]
    :type requiredProperties: list[str]
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


# noinspection PyPep8Naming
class ScenarioConductorConfiguration:
    """
    :type sessionIdentifier: str
    :type server: MartiServer
    :type clients: list[ATAKLiteClient]
    :type minimumRunTimeMS: int
    """

    def __init__(self,
                 sessionIdentifier,
                 server,
                 clients,
                 minimumRunTimeMS,
                 ):
        self.sessionIdentifier = sessionIdentifier
        self.server = server
        self.clients = clients
        self.minimumRunTimeMS = minimumRunTimeMS
        self.server.parent_config = self
        for c in self.clients:
            c.parent_config = self

    def to_dict(self):
        return {
            'sessionIdentifier': self.sessionIdentifier,
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
