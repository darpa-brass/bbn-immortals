#!/usr/bin/env python2.7

import argparse
import logging

from scenarioconductor.testing import ll_dummy_server as lds

_parser = argparse.ArgumentParser(prog='./testing', description='IMMoRTALS Testing Utility')
_sub_parsers = _parser.add_subparsers(help='Available Commands')

llds_parser = _sub_parsers.add_parser('llds', help='IMMoRTALS Mock LL TestHarness')

ll_dummy_server_parser = _sub_parsers.add_parser('llds', help=lds.parser.description)
lds.add_parser_arguments(ll_dummy_server_parser)
ll_dummy_server_parser.set_defaults(func=lds.main)

network_logger = logging.getLogger


def main():
    args = _parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()
