from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class SecurityStandard(Enum):
    def __init__(self, description: str):
        self.description = description  # type: str

    Nothing = (
        "No security required")

    FIPS140Dash1 = (
        "Obsolete less secure NIST government security standard")

    FIPS140Dash2 = (
        "Current secure NIST government security standard")

    NIST800Dash171 = (
        "Recent governemnt contractor security standard")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
