"""
Automatic generation of XSLT files
"""

from lxml import etree as et

import constants


def build_ast(namespaces, include_comments=False):
    initial_namespaces = {
        None: constants.HTML_NS,
        'xsl': constants.XSL_NS,
        'xsi': constants.XSI_NS,
        'my': 'http://233analytics.com/my'
    }

    # Merge namespaces
    for k, v in namespaces.items():
        if v in initial_namespaces:
            continue

        initial_namespaces[v] = k

    root_node = et.Element(
        et.QName(constants.XSL_NS, 'stylesheet'),
        version='2.0',
        nsmap=initial_namespaces,
    )
    gen_id_params = {'name': "my:gen-id", 'as': "xsd:string"}
    function_gen_id = et.SubElement(
        root_node,
        et.QName(constants.XSL_NS, 'function'),
        **gen_id_params
    )

    et.SubElement(
        function_gen_id,
        et.QName(constants.XSL_NS, 'sequence'),
        select="generate-id(my:gen-id-getnode())"
    )

    node_fx_params = {"name": "my:gen-id-getnode", "as": "element()"}
    node_fxn = et.SubElement(
        root_node,
        et.QName(constants.XSL_NS, 'function'),
        **node_fx_params
    )

    et.SubElement(
        node_fxn,
        et.QName(constants.HTML_NS, "node")
    )

    # include initial comments if requested by user
    if include_comments:
        # add copyright notice and some extra comments
        root_node.addprevious(et.Comment(constants.XSL_GENERATION_EXTRA_COMMENT))

    # To add comments somewhere in the code read:
    # . https://stackoverflow.com/questions/36320484/adding-comments-to-xml-documents

    # add root node to AST
    ast = et.ElementTree(root_node)

    return ast


def generate_xslt(first_tree, second_tree, compare_result, namespaces, location):
    # build XSLT file AST
    ast = build_ast(namespaces)

    # get root node of tree
    ast_root_node = ast.getroot()


    to_ignore = set(compare_result['additions']) & set(compare_result['removals'])

    additions = [i for i in compare_result['additions'] if i not in to_ignore]
    removals = [i for i in compare_result['removals'] if i not in to_ignore]

    relocations, additions_with_relocation = _fetch_additions_with_relocations(compare_result)

    # copy/maintain all content by default
    handle_default(ast_root_node, first_tree, namespaces, location)
    handle_additions(ast_root_node, additions, additions_with_relocation, first_tree, second_tree, namespaces)
    handle_renames(ast_root_node, compare_result['renames'], first_tree, namespaces)
    handle_removals(ast_root_node, removals, first_tree, namespaces)
    handle_relocations(ast_root_node, relocations, first_tree, second_tree, namespaces)

    sheet_bytes = et.tostring(ast, xml_declaration=True, pretty_print=True, encoding='utf-8')
    return sheet_bytes.decode()


def _fetch_additions_with_relocations(compare_result):
    additions = set(compare_result['additions'])

    relocations = set()

    for addition in additions:
        for relocation in compare_result['relocations']:
            _, rel_to = relocation
            if rel_to.startswith(addition):
                relocations.add(relocation)
                continue

    new_relocations = set(compare_result['relocations']) - relocations

    return new_relocations, relocations


def handle_default(ast_root_node, first_tree, namespaces, location):
    template_node = et.SubElement(
        ast_root_node,
        et.QName(constants.XSL_NS, 'template'),
        match='@*|node()',
    )

    copy_node = et.SubElement(
        template_node,
        et.QName(constants.XSL_NS, 'copy')
    )

    et.SubElement(
        copy_node,
        et.QName(constants.XSL_NS, 'apply-templates'),
        select='@*|node()'
    )

    # add comments and extra info
    template_node.addprevious(et.Comment(' identity template, copies everything as is '))

    rootName = first_tree.lxml_name(namespaces)

    # Remove target namespace
    # remove_schema_attr = et.SubElement(
    #     ast_root_node,
    #     et.QName(constants.XSL_NS, 'template'),
    #     match=f'{rootName}/@xsi:schemaLocation',
    # )
    #
    # remove_schema_attr.addprevious(et.Comment('Remove schemaLocation attr'))
    handle_relocate_document(ast_root_node, location)


def handle_relocate_document(parent_node, location):

    template_node = et.SubElement(
        parent_node,
        et.QName(constants.XSL_NS, 'template'),
        match='/mdl:MDLRoot/@xsi:schemaLocation'
    )

    attribute_node = et.SubElement(
        template_node,
        et.QName(constants.XSL_NS, 'attribute'),
        name='xsi:schemaLocation'
    )

    attribute_node.text = 'http://inetprogram.org/projects/MDL/ ' + location


def handle_additions(ast_root_node, additions, relocations, first_tree, second_tree, namespaces):
    for addition in sorted(additions):
        # Set relocations
        for rel_from, rel_to in relocations:
            if rel_to.startswith(addition):
                *_, element = _path_as_nodes(rel_to, second_tree)
                element.copy_of = _path_with_namespaces(rel_from, first_tree, namespaces)

        # Get parent element of our addition
        *path_addition, _ = addition.split('/')

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=_path_with_namespaces('/'.join(path_addition), second_tree, namespaces)
        )

        copy_node = et.SubElement(
            template_node,
            et.QName(constants.XSL_NS, 'copy')
        )

        et.SubElement(
            copy_node,
            et.QName(constants.XSL_NS, 'apply-templates'),
            select='@*|node()')

        *_, element = _path_as_nodes(addition, second_tree)

        element.to_lxml_element(copy_node)

        template_node.addprevious(et.Comment(f' Add node "{addition}" '))


def handle_renames(ast_root_node, renames, first_tree, namespaces):
    for renames in sorted(renames, key=lambda x: x[0]):
        from_path, to_path = renames

        *_, new_name = to_path.split('/')

        old_path = _path_with_namespaces(from_path, first_tree, namespaces)

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=old_path,
        )

        element_node = et.SubElement(
            template_node,
            et.QName(constants.XSL_NS, 'element'),
            name=old_path.split("/")[len(old_path.split("/")) - 1].split(":")[0] + ":" + new_name
        )

        et.SubElement(
            element_node,
            et.QName(constants.XSL_NS, 'apply-templates'),
            select='@*|node()'
        )

        template_node.addprevious(et.Comment(f' Renaming element "{from_path}" to "{new_name}"'))


def handle_removals(ast_root_node, removals, first_tree, namespaces):
    for i, removal in enumerate(sorted(removals)):
        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=_path_with_namespaces(removal, first_tree, namespaces),
        )

        if i == 0:
            template_node.addprevious(et.Comment(' handle removals'))


def handle_relocations(ast_root_node, relocations, first_tree, second_tree, namespaces):
    for rel_from, rel_to in sorted(relocations, key=lambda x: x[0]):
        # Removing element
        removal_template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=_path_with_namespaces(rel_from, first_tree, namespaces),
        )

        removal_template_node.addprevious(et.Comment(f' Moving element from "{rel_from}" to "{rel_to}"'))

        # Get parent element of our addition
        *path_to, _ = rel_to.split('/')

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=_path_with_namespaces('/'.join(path_to), second_tree, namespaces),
        )

        copy_node = et.SubElement(
            template_node,
            et.QName(constants.XSL_NS, 'copy')
        )

        et.SubElement(
            copy_node,
            et.QName(constants.XSL_NS, 'apply-templates'),
            select='@*|node()'
        )

        et.SubElement(
            copy_node,
            et.QName(constants.XSL_NS, 'copy-of'),
            select=_path_with_namespaces(rel_from, first_tree, namespaces)
        )


def _path_with_namespaces(path, second_tree, namespaces):
    new_path = []

    for i in _path_as_nodes(path, second_tree):
        new_path.append(i.lxml_name(namespaces))

    return '/' + '/'.join(new_path)


def _path_as_nodes(path, element_tree):
    root_node_name, *node_names = path.strip('/').split('/')

    current_node = element_tree

    # Always check root_node_Name if it's equal to our current_node
    assert current_node.name == root_node_name

    yield current_node

    for node_name in node_names:
        for child in current_node.children:
            if child.name == node_name:
                yield child
                current_node = child
