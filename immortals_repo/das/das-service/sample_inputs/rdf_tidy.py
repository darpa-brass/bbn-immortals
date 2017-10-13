

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

parser = argparse.ArgumentParser(description='Produce a Scenario Description File')
# parser.add_argument('--stdin','-c',
#                    dest='stdin',
#                    action='store_true',
#                    help='ignore --input and use "stdin".')

parser.add_argument('--input','-i',
                    dest='inputUrl',
                    type=argparse.FileType('r'),
                    default='input.ttl',
                    help='the input RDF/TTL file.')

parser.add_argument('--output','-o',
                    dest='outputUrl',
                    # type=argparse.FileType('wb', 0),
                    default='output.ttl',
                    help='the output RDF/TTL file.')

args = parser.parse_args()
print("arguments input:%s" % args.inputUrl)

g = rdflib.Graph()
result = g.parse(args.inputUrl, format="n3")

print("graph input has %s statements." % len(g))

# Now we write the result.
s = g.serialize(format='n3')

fd = open(args.outputUrl, 'wb')
fd.write(s)
fd.close()
