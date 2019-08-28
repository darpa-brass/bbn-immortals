import copy
import os
from typing import Dict, Optional

__title__ = 'IMMoRTALS XML Tester'
__author__ = "Austin Wellman"
__copyright__ = "Copyright 2019 Raytheon BBN Technologies"

XMLTEST_DIRECTORY = os.path.abspath(os.path.dirname(os.path.realpath(__file__)))
IMMORTALS_ROOT = os.path.abspath(os.path.join(XMLTEST_DIRECTORY, '../../../'))

config = None  # type: Configuration


class Configuration:
    def __init__(self, test: bool, build: bool, sanity_test: Optional[bool] = False,
                 results_path: Optional[str] = None, build_path: Optional[str] = None):
        self.test = test
        self.build = build
        self.sanity_test = sanity_test
        if results_path is None:
            self.results_path = results_path
        else:
            self.results_path = os.path.abspath(os.path.join(IMMORTALS_ROOT, 'TEST_RESULTS'))
        if build_path is None:
            self.build_path = os.path.abspath(os.path.join(XMLTEST_DIRECTORY, 'resources', 'output'))
        else:
            self.build_path = build_path


def init_config(value_dict: Dict) -> Configuration:
    global config
    value_dict_copy = copy.deepcopy(value_dict)
    value_dict_copy.pop('cmd')
    config = Configuration(**value_dict_copy)
    return config


from xmltest.__main__ import init_parser, main
