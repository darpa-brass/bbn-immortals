#!/usr/bin/env python3

import argparse
import os

_parser = None
_subparsers = None

ns = 'http://www.wsmr.army.mil/RCC/schemas/MDL'

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS MDL Tools', add_help=True)
    else:
        _parser = parent_parser.add_parser('mdl', help='IMMoRTALS MDL Tools')

    _subparsers = _parser.add_subparsers(dest='selected_parser')

    validation_parser = _subparsers.add_parser('validate')

    validation_parser.add_argument('input_files', metavar='INPUT_FILES', type=str, nargs='+',
                                   help='The XML files to validate')


def _fix(src_file: str):
    from lxml import etree
    doc = etree.parse(src_file)
    generic_parameters = doc.xpath('//GenericParameter')  # , namespaces={"mdl": ns})

    for gp in generic_parameters:
        parent = gp.getparent()
        children = parent.getchildren()

        name_child = None
        desc_child = None
        for child in children:
            if child.tag == 'Name':  # ('{' + ns + '}' + 'Name'):
                name_child = child

            elif child.tag == 'Description':  # ('{' + ns + '}' + 'Description'):
                desc_child = child

        parent.remove(gp)

        if desc_child is not None:
            parent.remove(desc_child)

        if name_child is not None:
            parent.remove(name_child)

        parent.insert(0, gp)

        if desc_child is not None:
            parent.insert(0, desc_child)

        if name_child is not None:
            parent.insert(0, name_child)

    doc.write(src_file)


def main(parser_args):
    if parser_args.selected_parser == 'validate':

        for src_file in parser_args.input_files:
            print("FILE: " + src_file)

            schema = etree.XMLSchema(
                etree.parse(
                    os.path.join(SCRIPT_DIRECTORY, 'resources', 'xsd', 'MDL_v1_0_0-bbn.xsd')))

            doc = etree.parse(src_file)

            if not schema.validate(doc):
                print('STATUS: INVALID')
                print("DETAILS:")
                schema.assertValid(doc)
            else:
                print('STATUS: VALID')

    else:
        _parser.print_help()


if __name__ == '__main__':
    init_parser()
    main(_parser.parse_args())
