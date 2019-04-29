"""

Common utility functions.

Author: Di Yao (di.yao@vanderbilt.edu)
"""

def strip_trailing_a(tag):
    if (tag.endswith("_a")):
        return tag[:len(tag)-2]

    return tag