import argparse
import logging

import ll_dummy_server as lds

parser = argparse.ArgumentParser(prog='testing')
subparsers = parser.add_subparsers(help='Available Commands')

ll_dummy_server_parser = subparsers.add_parser('llds', help=lds.parser.description)
lds.add_parser_arguments(ll_dummy_server_parser)
ll_dummy_server_parser.set_defaults(func=lds.main)

network_logger = logging.getLogger


def main():
    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()
