__title__ = 'immortals'
__author__ = "Austin Wellman"
__copyright__ = "Copyright 2016 Raytheon BBN Technologies"


def genapis():
    from pymmortals import api_gen
    api_gen.generate_apis()


def pojoize():
    from pymmortals import pojoizer
    pojoizer.pojoize_immortals_repo()
