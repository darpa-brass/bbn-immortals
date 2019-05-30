from xml.dom import minidom
from flask import Flask
from flask import request
from flask import jsonify

from generator import generate_xslt_file
from mapping.xsdentity import find_entities
from mapping.xsdmapper import Mapper

import time as time___

app = Flask(__name__)


@app.route('/xsdsts/ping')
def ping():
    return str(int(round(time___.time() * 1000)))


@app.route('/xsdsts/translate', methods=['POST'])
def get_xslt():
    if not request.is_json:
        return jsonify({"error": 'Bad Request', "xslt": ""})

    data = request.get_json()

    mapper = Mapper()
    src_result = {}
    for d in data["srcSchema"]["documents"]:
        src_result = {**src_result, **find_entities(minidom.parseString(d["documentContent"]))}

    dst_result = {}
    for d in data["dstSchema"]["documents"]:
        dst_result = {**dst_result, **find_entities(minidom.parseString(d["documentContent"]))}
        if "MDL" in d['documentName']:
            mapper.new_schema_location = d['documentName']

    mapper.map_from_entities(src_result, dst_result)

    result = generate_xslt_file(mapper)

    old_field_count = len(mapper.old_fields)
    new_field_count = len(mapper.new_fields)
    unchanged_count = len(mapper.unchanged)
    renamed_count = len(mapper.renamed)
    typerenamed_count = len(mapper.typerenamed)
    removed_count = len(mapper.removed)
    added_count = len(mapper.added)
    graph_similar = len(mapper.removed_graph_similar)
    add_graph_similar = len(mapper.added_graph_similar)
    similar_doc = len(mapper.similar_by_doc)

    print(f" > Old field count: {old_field_count}")
    print(f" > New field count: {new_field_count}")
    print(f" > Unchanged field count: {unchanged_count}")
    print(f" > Removed fields (present in old and not in new): {removed_count}")
    print(f" > Added fields (present in new and not in old): {added_count}")
    print(f" > Fields with small diff in name: {renamed_count}")
    print(f" > Fields with small diff in type name: {typerenamed_count}")
    print(f" > Removed fields with similar graphs: {graph_similar}")
    print(f" > Added fields with similar graphs: {add_graph_similar}")
    print(f" > Similar documentation: {similar_doc}")
    print("")
    print(f" > Percent of fields mapped ((added/new_fields) + (removed/old_fields))/2: {mapper.mapping_percent}")

    return jsonify({
        "xslt": result,
        "translationMetrics": {
            "oldFieldCount": old_field_count,
            "newFieldCount": new_field_count,
            "removedFields": removed_count,
            "addedFields": added_count,
            "renamedFields": renamed_count,
            "renamedTypes": typerenamed_count,
            "graphSimilarRemoved": graph_similar,
            "graphSimilarAdded": add_graph_similar,
            "similarDocumentation": similar_doc,
            "percentMapped": mapper.mapping_percent
        }
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8090)
