import json
from xml.dom import minidom
from flask import Flask
from flask import request
from flask import jsonify

from translation.xsddiff import get_all_fields, diff_to_mapping

import time as time___

app = Flask(__name__)


@app.route('/xsdsts/ping')
def ping():
	return str(int(round(time___.time() * 1000)))

@app.route('/xsdsts/translate', methods=['POST'])
def get_xslt():
    if not request.is_json:
        return 'Bad Request'

    data = request.get_json()

    srcfields = []
    for d in data["srcSchema"]["documents"]:
        srcfields.extend(get_all_fields(minidom.parseString(d["documentContent"])))

    destfields = []
    for d in data["dstSchema"]["documents"]:
        destfields.extend(get_all_fields(minidom.parseString(d["documentContent"])))

    deleted_result, xslt_result = diff_to_mapping(srcfields, destfields, srcfields)

    return jsonify({"error": deleted_result, "xslt": xslt_result})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8090)
