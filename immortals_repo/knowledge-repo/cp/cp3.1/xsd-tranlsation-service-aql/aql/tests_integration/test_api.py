import json
from xml.dom import minidom

from pytest import fixture, mark

from server import app


@fixture
def request_json(shared_datadir):
    with open(shared_datadir / 'v1-to-v2-request.json', 'r') as request_json:
        return request_json.read()


@fixture
def client():
    return app.test_client()


def test_api(client, request_json):
    response = client.post('/xsdsts/translate',
                           data=request_json,
                           content_type='application/json')

    assert response.status_code == 200
    result = response.get_json()

    assert 'xslt' in result

    document = minidom.parseString(result['xslt'])

    template_tags = document.getElementsByTagName('xsl:template')

    # For now this is the number with improvements of course it must change
    assert len(template_tags) == 157

    # Checking some arbitrary data

    matches = {i.getAttribute('match') for i in template_tags}

    # Additions
    assert '/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter' in matches

    # Removals
    assert '/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Ports' in matches

    # A structural change
    assert '/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode' in matches


@mark.parametrize('field', ['srcSchema', 'dstSchema'])
def test_api_returns_bad_request_if_no_primary_is_defined_in_source(client, request_json, field):
    json_content = json.loads(request_json)

    for doc in json_content[field]['documents']:
        doc['primarySchemaDoc'] = False

    response = client.post('/xsdsts/translate', json=json_content)

    assert response.status_code == 400
    result = response.get_json()

    assert field in result['error']
