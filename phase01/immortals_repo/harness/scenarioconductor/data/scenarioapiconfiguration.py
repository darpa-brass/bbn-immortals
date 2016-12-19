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

    def equals(self, other):
        """
        :param MartiServer other:
        """
        return self.bandwidth == other.bandwidth


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

    def equals(self, other):
        """
        :param  ATAKLiteClient other:
        """
        isEqual = self.imageBroadcastIntervalMS == other.imageBroadcastIntervalMS \
                  and self.latestSABroadcastIntervalMS == other.latestSABroadcastIntervalMS \
                  and self.count == other.count

        if not isEqual or len(self.presentResources) != len(other.presentResources) \
                or len(self.requiredProperties) != len(other.requiredProperties):
            return False

        else:
            for res in self.presentResources:
                if not res in other.presentResources:
                    return False

            for prop in self.requiredProperties:
                if not prop in other.requiredProperties:
                    return False

        return True


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

    def equals(self, other):
        """
        :param ScenarioConductorConfiguration other:
        :rtype: bool
        """

        return self.server.equals(other.server) and len(self.clients) == 1 and len(other.clients) == 1 and self.clients[
            0].equals(other.clients[0]) and self.minimumRunTimeMS == other.minimumRunTimeMS
