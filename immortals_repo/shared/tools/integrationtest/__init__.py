__title__ = 'immortals'
__author__ = "Austin Wellman"
__copyright__ = "Copyright 2016 Raytheon BBN Technologies"

from integrationtest.__main__ import init_parser, main


def genapis():
    from integrationtest import api_gen
    api_gen.generate_apis()


def pojoize():
    from integrationtest import pojoizer
    pojoizer.pojoize_immortals_repo()
