from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class SecurityStandard(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    Nothing = ("Nothing",
        "No security required")

    FIPS140Dash1 = ("FIPS140Dash1",
        "Obsolete less secure NIST government security standard")

    FIPS140Dash2 = ("FIPS140Dash2",
        "Current secure NIST government security standard")

    NIST800Dash171 = ("NIST800Dash171",
        "Recent governemnt contractor security standard")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
