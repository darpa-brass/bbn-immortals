from copy import deepcopy


# noinspection PyPep8Naming
class LocationBehaviorProfile:
    """
    :type country: str
    :type direction: str
    :type degreeChangePerSecond: float
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**deepcopy(d))

    def __init__(self, country, direction, degreeChangePerSecond):
        self.country = country
        self.direction = direction
        self.degreeChangePerSecond = degreeChangePerSecond

    def to_dict(self):
        return deepcopy(self.__dict__)


# noinspection PyPep8Naming
class SimulatedEnvironment:
    """
    :type availableResources: list[str]
    :type locationBehaviorProfiles: dict[str,LocationBehaviorProfile]
    """

    @classmethod
    def from_dict(cls, d):
        dc = deepcopy(d)
        dc['locationBehaviorProfiles'] = {k: LocationBehaviorProfile.from_dict(d[k]) for k in
                                          d['locationBehaviorProfiles']}
        return cls(**dc)

    def __init__(self, availableResources, locationBehaviorProfiles):
        self.availableResources = availableResources
        self.locationBehaviorProfiles = locationBehaviorProfiles

    def to_dict(self):
        return deepcopy(self.__dict__)
