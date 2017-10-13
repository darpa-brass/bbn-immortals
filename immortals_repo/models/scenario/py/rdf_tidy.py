

# This python script which will take an RDF/TTL file and
# improve the serialization.

# It follows python argparse syntax.

# e.g.
# cat ./prefixes.ttl ./gme-output-sample1.ttl > input.ttl
# python3 ./rdf_tidy.py --input input.ttl --output output.ttl
#
# or
#
# cat ./prefixes.ttl ./input.ttl | python3 ./rdf_tidy.py --input -
#
# easy_install rdflib
#
# Secondary libaries:
# https://docs.python.org/3.3/library/argparse.html
#

import argparse
import rdflib

PARSER = argparse.ArgumentParser(
    description='Produce a Scenario Description File')

PARSER.add_argument('--input', '-i',
                    dest='inputUrl',
                    type=argparse.FileType('r'),
                    default='input.ttl',
                    help='the input RDF/TTL file.')

PARSER.add_argument('--output', '-o',
                    dest='outputUrl',
                    # type=argparse.FileType('wb', 0),
                    default='output.ttl',
                    help='the output RDF/TTL file.')

ARGS = PARSER.parse_args()
print("arguments input: {}".format(ARGS.inputUrl))

G = rdflib.Graph()
G.parse(ARGS.inputUrl, format="n3")

print("graph input has {} statements.".format(len(G)))

# Now we write the result.
S = G.serialize(format='n3')

FD = open(ARGS.outputUrl, 'wb')
FD.write(S)
FD.close()
