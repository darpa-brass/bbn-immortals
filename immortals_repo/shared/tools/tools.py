#!/usr/bin/env python3

import argparse

import buildsystem
import installer
import integrationtest
import odbhelper

_parser = argparse.ArgumentParser()
_subparsers = _parser.add_subparsers(dest='cmd')

if __name__ == '__main__':
    buildsystem_parser = buildsystem.init_parser(_subparsers)
    integrationtest_parser = integrationtest.init_parser(_subparsers)
    installer_parser = installer.init_parser(_subparsers)
    odbhelper_parser = odbhelper.init_parser(_subparsers)
    args = _parser.parse_args()

    if args.cmd is None:
        _parser.print_help()
    elif args.cmd == 'buildsystem':
        buildsystem.main(args)
    elif args.cmd == 'integrationtest':
        integrationtest.main(args)
    elif args.cmd == 'installer':
        installer.main(args)
    elif args.cmd == 'odbhelper':
        odbhelper.main(args)
