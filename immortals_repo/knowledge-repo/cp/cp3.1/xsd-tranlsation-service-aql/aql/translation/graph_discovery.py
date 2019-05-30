def cyclic(g):
    """Return True if the directed graph g has a cycle.
    g must be represented as a dictionary mapping vertices to
    iterables of neighbouring vertices. For example:

    >>> cyclic({1: (2,), 2: (3,), 3: (1,)})
    True
    >>> cyclic({1: (2,), 2: (3,), 3: (4,)})
    False

    """
    path = []
    visited = set()

    def visit(vertex):
        if vertex in visited:
            return False
        visited.add(vertex)
        path.append(vertex)
        for neighbour in g.get(vertex, ()):
            if neighbour in path or visit(neighbour):
                for p in path:
                    print(f"{p}")
                print("------------------------")
                return True
        path.remove(vertex)
        return False

    print("Done")

    return any(visit(v) for v in g)


def find_graphs(document):
    """
    Returns the difference in complex types from one version of the schema to another
    :param srcdoc: Parsed source XSD
    :param destdoc: Parsed destiniy XSD
    :return: The added and removed entities between two versions
    """
    fields = []

    connections = []

    graph = {}

    edges_strs = []

    for n in document.getElementsByTagName("xsd:complexType"):
        name = n.getAttribute('name')

        fields = get_fields(n, "xsd:element", n)
        edges = []
        for f in fields:
            a = f'"{f.name}" -> "{f.field_type}"'
            if a not in edges_strs:
                print(a)
                edges_strs.append(a)
            if f.field_type in XSD_TYPES:
                continue
            if f.field_type not in edges:
                edges.append(f.field_type)

        graph[name] = edges

    # print(cyclic(graph))