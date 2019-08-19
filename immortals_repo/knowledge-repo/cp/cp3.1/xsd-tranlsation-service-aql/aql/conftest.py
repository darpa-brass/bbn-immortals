from pytest import fixture

from mappers.parser import SchemaRepository


@fixture
def build_repository():
    def _build_repository(documents):
        repo = SchemaRepository()

        for doc in documents:
            repo.add(doc['name'],
                     doc['content'],
                     primary=doc.get('primarySchemaDoc', False))

        return repo

    return _build_repository


@fixture
def get_main_element(build_repository):
    def _get_main_element(documents):
        return build_repository(documents).get_main_element()

    return _get_main_element


@fixture
def get_main_element_single(get_main_element):
    def _get_main_element_single(doc_content):
        return get_main_element(
            [{'name': 'single-file.xsd',
              'content': doc_content,
              'primarySchemaDoc': True}]
        )

    return _get_main_element_single
