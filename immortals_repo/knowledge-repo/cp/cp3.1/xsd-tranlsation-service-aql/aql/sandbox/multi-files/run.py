from itertools import chain
from collections import defaultdict
from xml.dom import minidom
import json


src_docs = defaultdict(set)
dst_docs = defaultdict(set)


# with open('./v1 to v2.request.json', 'r') as json_file:
#     result = json.loads(json_file.read())

#     for i in result['srcSchema']['documents']:
#         with open('./src-files/{}'.format(i['documentName']), 'w') as f:
#             f.write(i['documentContent'])

#     for i in result['dstSchema']['documents']:
#         with open('./dst-files/{}'.format(i['documentName']), 'w') as f:
#             f.write(i['documentContent'])

#     print(result.keys())

with open('./v1 to v2.request.json', 'r') as json_file:
    result = json.loads(json_file.read())

    for i in result['srcSchema']['documents']:
        dom = minidom.parseString(i['documentContent'])
        for n in chain(dom.getElementsByTagName('xsd:import'), dom.getElementsByTagName('xs:import')):
            src_docs[n.getAttribute('schemaLocation')].add(i['documentName'])
            print('xsd:', i['documentName'], n.getAttribute('schemaLocation'))

        for n in chain(dom.getElementsByTagName('xs:complexType'), dom.getElementsByTagName('xsd:complexType')):
            print('SRC - File:', i['documentName'], 'NAME:', n.getAttribute('name'))

        # with open('./src-files/{}'.format(i['documentName']), 'w') as f:
        #     f.write(i['documentContent'])

    for i in result['dstSchema']['documents']:
        dom = minidom.parseString(i['documentContent'])
        for n in chain(dom.getElementsByTagName('xsd:import'), dom.getElementsByTagName('xs:import')):
            dst_docs[n.getAttribute('schemaLocation')].add(i['documentName'])
            print('xsd:', i['documentName'], n.getAttribute('schemaLocation'))

        for n in chain(dom.getElementsByTagName('xs:complexType'), dom.getElementsByTagName('xsd:complexType')):
            print('DST - File:', i['documentName'], 'NAME:', n.getAttribute('name'))

    src_docs = dict(src_docs)
    dst_docs = dict(dst_docs)

    for i in result['srcSchema']['documents']:
        if i['documentName'] in src_docs:
            continue

        src_docs[i['documentName']] = set()

    for i in result['dstSchema']['documents']:
        if i['documentName'] in dst_docs:
            continue

        dst_docs[i['documentName']] = set()


import pprint
pprint.pprint(src_docs)
pprint.pprint(dst_docs)
