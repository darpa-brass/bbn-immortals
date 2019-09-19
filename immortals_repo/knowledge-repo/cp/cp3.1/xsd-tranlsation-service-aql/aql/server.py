from flask import Flask
from flask import request
from flask import jsonify

from generator import generate_xslt
from mappers.tree import recalculate_hashes
from mappers.diff import compare
from mappers.parser import SchemaRepository
import log_config

import time as time___

app = Flask(__name__)


log_config.setup()


@app.route('/xsdsts/ping')
def ping():
    return str(int(round(time___.time() * 1000)))


def _build_repository(documents):
    repo = SchemaRepository()

    for doc in documents:
        repo.add(doc['documentName'],
                 doc['documentContent'],
                 primary=doc.get('primarySchemaDoc', False))

    return repo


@app.route('/xsdsts/translate', methods=['POST'])
def get_xslt():
    if not request.is_json:
        return jsonify({'error': 'Bad Request, json expected', 'xslt': ''}), 400

    data = request.get_json()

    if 'srcSchema' not in data:
        return jsonify({'error': 'Bad Request, "srcSchema" expected', 'xslt': ''}), 400

    if 'dstSchema' not in data:
        return jsonify({'error': 'Bad Request, "dstSchema" expected', 'xslt': ''}), 400

    if 'documents' not in data['srcSchema']:
        return jsonify({'error': 'Bad Request, "srcSchema/documents" expected', 'xslt': ''}), 400

    if 'documents' not in data['dstSchema']:
        return jsonify({'error': 'Bad Request, "dstSchema/documents" expected', 'xslt': ''}), 400

    if len(data['srcSchema']['documents']) == 0:
        return jsonify({'error': 'Bad Request, expected at least one document', 'xslt': ''}), 400

    if len(data['dstSchema']['documents']) == 0:
        return jsonify({'error': 'Bad Request, expected at least one document', 'xslt': ''}), 400

    repo_src = _build_repository(data['srcSchema']['documents'])
    repo_dst = _build_repository(data['dstSchema']['documents'])

    src_result = repo_src.get_main_element()
    dst_result = repo_dst.get_main_element()

    if not src_result:
        return jsonify({
            'error': 'Bad Request, expected at least one document as primary in "srcSchema"',
            'xslt': ''}), 400

    if not dst_result:
        return jsonify({
            'error': 'Bad Request, expected at least one document as primary in "dstSchema"',
            'xslt': ''}), 400

    src_element = src_result['element']
    dst_element = dst_result['element']

    recalculate_hashes(src_element)
    recalculate_hashes(dst_element)

    compare_result = compare(src_element, dst_element)

    xslt = generate_xslt(src_element,
                         dst_element,
                         compare_result,
                         src_result['namespaces'],
                         dst_result['location'])

    # TODO
    old_field_count = None
    new_field_count = None
    unchanged_count = None
    typerenamed_count = None
    graph_similar = None
    add_graph_similar = None
    similar_doc = None

    # TODO (handle nodes in additions and removals)
    removed_count = None
    added_count = None

    renamed_count = len(compare_result['renames'])

    print(f' > Old field count: {old_field_count}')
    print(f' > New field count: {new_field_count}')
    print(f' > Unchanged field count: {unchanged_count}')
    print(f' > Removed fields (present in old and not in new): {removed_count}')
    print(f' > Added fields (present in new and not in old): {added_count}')
    print(f' > Fields with small diff in name: {renamed_count}')
    print(f' > Fields with small diff in type name: {typerenamed_count}')
    print(f' > Removed fields with similar graphs: {graph_similar}')
    print(f' > Added fields with similar graphs: {add_graph_similar}')
    print(f' > Similar documentation: {similar_doc}')
    print('')
    print(f' > Percent of fields mapped ((added/new_fields) + (removed/old_fields))/2: None')

    return jsonify({
        'xslt': xslt,
        'translationMetrics': {
            'oldFieldCount': old_field_count,
            'newFieldCount': new_field_count,
            'removedFields': removed_count,
            'addedFields': added_count,
            'renamedFields': renamed_count,
            'renamedTypes': typerenamed_count,
            'graphSimilarRemoved': graph_similar,
            'graphSimilarAdded': add_graph_similar,
            'similarDocumentation': similar_doc,
            'percentMapped': None  # TODO
        }
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8090)
