#!/usr/bin/env python3
import argparse

from integrationtest import resources

_parser = None


def init_parser(parent_parser=None):
    global _parser

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS Integration Test Runner')
    else:
        _parser = parent_parser.add_parser('integrationtest', help='IMMoRTALS Integration Test Runner')

    _parser.add_argument('test', metavar='TEST',
                         choices=resources.get_test_and_test_suite_list())

    _parser.add_argument('-r', '--immortals-root', type=str)


def main(parser_args):
    from integrationtest.testing.systemvalidator import SystemValidator

    sv = SystemValidator(immortals_root=parser_args.immortals_root)
    test_identifier = parser_args.test
    test_suite_identifier = resources.get_parent_test_suite_identifier(parser_args.test)

    if test_identifier == test_suite_identifier:
        sv.start(test_suite_identifier=test_suite_identifier)
    else:
        sv.start(test_suite_identifier=test_suite_identifier, test_identifier=test_identifier)
