#!/usr/bin/env python3

import os
from typing import List


def bundle(input_filepath: str, output_filepath: str, extension: str):
    lines = list()  # type: List[str]
    ext = '.' + extension

    for path, dirs, files in os.walk(input_filepath):
        for filename in files:
            if filename.endswith(ext):
                with open(os.path.join(path, filename), 'r') as file:
                    lines = lines + file.readlines()

    with open(output_filepath, 'w') as output:
        output.writelines(lines)


def main():
    ttl_folder = os.path.abspath(os.path.join(
        os.path.realpath(__file__), '../../../knowledge-repo/vocabulary/ontology-static/ontology'))
    target = os.path.abspath(os.path.join(os.path.realpath(__file__), '../../../uber.ttl'))
    bundle(ttl_folder, target, 'ttl')


if __name__ == '__main__':
    main()
