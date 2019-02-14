#!/usr/bin/env python3
import argparse
import os

from installer import datatypes
from installer.prepare import install

_parser = None


def init_parser(parent_parser=None):
    global _parser

    if parent_parser is None:
        _parser = argparse.ArgumentParser(description='IMMoRTALS Environment Installer')
    else:
        _parser = parent_parser.add_parser('installer', help='IMMoRTALS Environment Installer')

    _parser.add_argument('--installation-dir', action='store',
                         help="Specifies the directory to install dependencies to and runs in unattended mode.")

    _parser.add_argument("--profile", action='store',
                         help="The installation profile. The default value is phase3.",
                         choices=datatypes.get_configuration_profiles(), default="phase3")


def main(parser_args):
    if parser_args.installation_dir is not None:
        install_dir = parser_args.installation_dir
        if not install_dir.endswith('/'):
            install_dir += '/'

    else:
        install_dir = os.getenv('HOME') + '/.immortals/'
        try:
            val = input('Please enter a path for installing dependencies [' + install_dir + ']:')
        except SyntaxError or EOFError:
            val = install_dir

        if val is not "":
            if not val.endswith('/'):
                val += '/'
            install_dir = val

    install(install_dir=install_dir, configuration_profile=parser_args.profile)
