"""
Automatic generation of XSLT files
"""

from lxml import etree as et

import constants


def build_ast(include_comments=False):
    root_node = et.Element(
        et.QName(constants.XSL_NS, 'stylesheet'),
        version='1.0',
        nsmap={
            None: constants.HTML_NS,
            'mdl': constants.MDL_NS,
            'xsl': constants.XSL_NS,
            'xsi': constants.XSI_NS
        },
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


def generate_xslt(first_tree, second_tree, compare_result):
    # build XSLT file AST
    ast = build_ast()

    # get root node of tree
    ast_root_node = ast.getroot()

    to_ignore = set(compare_result['additions']) & set(compare_result['removals'])

    additions = [i for i in compare_result['additions'] if i not in to_ignore]
    removals = [i for i in compare_result['removals'] if i not in to_ignore]

    relocations, additions_with_relocation = _fetch_additions_with_relocations(compare_result)

    # copy/maintain all content by default
    handle_default(ast_root_node)
    handle_additions(ast_root_node, second_tree, additions, additions_with_relocation)
    handle_renames(ast_root_node, compare_result['renames'])
    handle_removals(ast_root_node, removals)
    handle_relocations(ast_root_node, relocations)

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


def handle_default(ast_root_node):
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


def handle_additions(ast_root_node, second_tree, additions, relocations):
    for addition in additions:
        *path, _ = addition.split('/')

        # Set relocations
        for rel_from, rel_to in relocations:
            if rel_to.startswith(addition):
                *_, element = _path_as_nodes(rel_to, second_tree)
                element.copy_of = rel_from

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match='/'.join(path)
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


def _path_as_nodes(path, second_tree):
    _, root_node_name, *node_names = path.split('/')

    current_node = second_tree

    # Always check root_node_Name if it's equal to our current_node
    assert current_node.name == root_node_name

    for node_name in node_names:
        for child in current_node.children:
            if child.name == node_name:
                yield child
                current_node = child


def handle_renames(ast_root_node, renames):
    for renames in renames:
        from_path, to_path = renames

        *_, new_name = to_path.split('/')

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=from_path,
        )

        element_node = et.SubElement(
            template_node,
            et.QName(constants.XSL_NS, 'element'),
            name=new_name
        )

        et.SubElement(
            element_node,
            et.QName(constants.XSL_NS, 'apply-templates'),
            select='@*|node()'
        )

        template_node.addprevious(et.Comment(f' Renaming element "{from_path}" to "{new_name}"'))


def handle_removals(ast_root_node, removals):
    for i, removal in enumerate(removals):
        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=removal,
        )

        if i == 0:
            template_node.addprevious(et.Comment(' handle removals'))


def handle_relocations(ast_root_node, relocations):
    for rel_from, rel_to in relocations:
        # Removing element
        removal_template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match=rel_from,
        )

        removal_template_node.addprevious(et.Comment(f' Moving element from "{rel_from}" to "{rel_to}"'))

        *path, _ = rel_to.split('/')

        template_node = et.SubElement(
            ast_root_node,
            et.QName(constants.XSL_NS, 'template'),
            match='/'.join(path),
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
            select=rel_from
        )
