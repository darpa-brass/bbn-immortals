import argparse

import immortalsglobals as ig
import scenarioconductor as sc
import scenariorunner as sr
from reporting import testharnessreporter
from testing import ll_dummy_server as lds

parser = argparse.ArgumentParser(prog='scenarioconductor')
subparsers = parser.add_subparsers(help='Available Commands')

sc_parser = subparsers.add_parser('scenarioconductor', help=sc.parser.description)
sc.add_parser_arguments(sc_parser)
sc_parser.set_defaults(func=sc.main)

sr_parser = subparsers.add_parser('scenariorunner', help=sr.parser.description)
sr.add_parser_arguments(sr_parser)
sr_parser.set_defaults(func=sr.main)

lds_parser = subparsers.add_parser('lds', help=lds.parser.description)
lds.add_parser_arguments(lds_parser)
lds_parser.set_defaults(func=lds.main)


def main():
    args = parser.parse_args()
    ig.set_logger(
        testharnessreporter.FakeTestHarnessReporter(ig.configuration.logRoot, ig.configuration.artifactRoot))
    args.func(args)


if __name__ == '__main__':
    main()
