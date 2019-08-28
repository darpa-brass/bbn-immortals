#!/usr/bin/env python3

import argparse
import os

from xmltest import init_config

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

_parser = None
_subparsers = None


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('xmltest', help='IMMoRTALS OrientDB/MDL Helper')

    _parser.add_argument('--test', action='store_true', help='Execute Testing')

    _parser.add_argument('--build', '-b', action='store_true',
                         help='Build scenarios and store them in the resource directory')

    _parser.add_argument('--build-path', '-p', type=str, help='Build scenarios and store them in the specified path')

    _parser.add_argument('--results-path', '-o', type=str, help='The target directory for the results')

    _parser.add_argument('--sanity-test', action='store_true', help='Performs a test on a standard MDL structure')


def main(parser_args):
    config = init_config(parser_args.__dict__)
    if not (config.test or config.build):
        _parser.print_help()
        exit(0)

    if config.build:
        from xmltest.builder import BasicBuilder
        from xmltest.fragments import XmlElement

        for scenario in XmlElement:
            bb = BasicBuilder(scenario, config.build_path)
            bb.build()
            bb.validate()

    if config.sanity_test:
        from xmltest.tester import XmlTester
        xt = XmlTester()
        xt.sanity_test()

    if config.test:
        from xmltest.tester import XmlTester
        xt = XmlTester()
        xt.execute()
        exit(0)


if __name__ == '__main__':
    init_parser()
    main(_parser.parse_args())
