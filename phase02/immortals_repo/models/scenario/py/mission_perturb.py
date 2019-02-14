#!/bin/python3
'''
This python script which will take the baseline TTL GME
interchange file and edit it based on the supplied parameters.

brass/immortals-svn/das/das-service/src/main/java/mil/darpa/immortals/core/das/sparql\

It follows python argparse syntax.

e.g.
python3 ./py/mission_perturb.py
     --output gmei.ttl
     --template ./scenario/py/gme-interchange-example-2.ttl
     --client-device-count 21

Some of these simply update a literal value objective on a triple:
--pli-server-msg-rate :

Others update a value and generate multiple tuples
--client-device-count :

Others indicate the presence or absence of a resource
--android-usb-available

Finally, some indicate mission constraints
--mission-req-trusted

The principle library used by this code is:
http://rdflib.readthedocs.io/en/stable/
It allows us to edit the TTL template as an RDF graph rather than text.
It also provides the SPARQL language.
http://rdflib3.readthedocs.io/en/latest/intro_to_sparql.html

sudo easy_install3 rdflib
...or...
sudo apt install python3-rdflib

Secondary libaries:
https://docs.python.org/3.3/library/argparse.html
https://docs.python.org/3/library/difflib.html
'''

from difflib import SequenceMatcher
import time
# from datetime import datetime
import argparse
# import uuid
import rdflib


def current_milli_time():
    '''
    This computes the current time in millisecs.
    '''
    seconds = int(round(time.time() * 1000))
    # millisec = datetime.now()
    return seconds


PARSER = argparse.ArgumentParser(
    description='Produce a Scenario Description File')
PARSER.add_argument('--template',
                    dest='templateUrl',
                    type=argparse.FileType('r'),
                    default='gmi.ttl.in',
                    help='the template RDF/TTL file.')

PARSER.add_argument('--output',
                    dest='outputUrl',
                    default='gmi.output.ttl',
                    help='the output RDF/TTL file.')

# Initially this defaulted to a uuid
# uuid.uuid1()
PARSER.add_argument('--session',
                    dest='sessionId',
                    default='I{}'.format(current_milli_time()),
                    help='the output RDF/TTL file.')


PARSER.add_argument('--pli-client-msg-rate',
                    dest='pliClientMsgRate',
                    default='10',  # {x | x is +R}
                    help='the PLI (Location) message rate leaving the client.')

PARSER.add_argument('--image-client-msg-rate',
                    dest='imageClientMsgRate',
                    default='1',  # {x | x is +R}
                    help='the image message rate leaving the client.')

PARSER.add_argument('--server-bandwidth',
                    dest='serverBandwidth',
                    default='1',  # {x | x is +R}
                    help='the bandwidth allowance to the server.')

PARSER.add_argument('--client-device-count',
                    dest='clientDeviceCount',
                    default='2',  # {x | x is N}
                    help='the number of Android client devices expected (and realized).')

PARSER.add_argument('--android-bluetooth-resource', '-b',
                    dest='androidBluetootRes',
                    default='yes',
                    help='the bluetooth protocol available on the Android. {absent active bt-le}')

PARSER.add_argument('--android-usb-resource',
                    dest='androidUsbRes',
                    default='yes',
                    help='''
                    what is the USB type on the Android.
                    {absent active on-the-go usb1 usb2 usb3}
                    ''')

PARSER.add_argument('--android-internal-gps-resource',
                    dest='androidInternalGpsRes',
                    default='yes',
                    help='what GPS internally available on the Android. {none single, multi}')

PARSER.add_argument('--android-ui-resource',
                    dest='androidUiRes',
                    default='yes',
                    help='''
                    is the bluetooth hardware available on the Android.
                    {available unavailable}
                    ''')

PARSER.add_argument('--gps-satellite-resource',
                    dest='gpsSatelliteRes',
                    default='yes',
                    help='''
                    is the GPS satellite cluster expected to be reachable
                    {unreachable reachable reliable}
                    ''')

PARSER.add_argument('--mission-trusted-comms',
                    dest='missionTrustComms',
                    default='yes',
                    help='does the mission require encrypted communications. {required use ignore}')

ARGS = PARSER.parse_args()
print("arguments template:{}".format(ARGS.templateUrl))

G = rdflib.Graph()
RESULT = G.parse(ARGS.templateUrl, format="n3")


print("graph has {} statements.".format(len(G)))

IMN = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0#")
G.bind("IMMoRTALS", IMN)

IMN_CP1 = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/cp#")
G.bind("IMMoRTALS_cp1", IMN_CP1)

IMN_CP2 = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/cp2#")

IMN_METRICS = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/metrics#")
G.bind("IMMoRTALS_metrics", IMN_METRICS)

IMN_ANDROID = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/android#")
G.bind("IMMoRTALS_android", IMN_ANDROID)

IMN_RESOURCES = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/resources#")
G.bind("IMMoRTALS_resources", IMN_RESOURCES)

IMN_RESOURCES_GPS = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/resources/gps#")
G.bind("IMMoRTALS_resources_gps", IMN_RESOURCES_GPS)

IMN_PROPERTY_IMPACT = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/property/impact#")
G.bind("IMMoRTALS_property_impact", IMN_PROPERTY_IMPACT)

IMN_PROPERTY_GPS_PROP = rdflib.Namespace(
    "http://darpa.mil/immortals/ontology/r2.0.0/resources/gps/properties#")
G.bind("IMMoRTALS_resources_gps_properties", IMN_PROPERTY_GPS_PROP)


def similar(word1, word2):
    '''
    compare two strings to see if they are similar
    '''
    return SequenceMatcher(None, word1, word2).ratio()


def remove_tuple(subject1, predicate1, objective1):
    '''
    remove a tuple from the graph
    '''
    G.remove((subject1, predicate1, objective1))


def keep_tuple(_subject1, _predicate1, _objective1):
    '''
    do not take an action on the tuple
    '''
    pass

HEADER_ARRAY = G.query(
    """SELECT DISTINCT ?subject
       WHERE {
           ?subject a IMMoRTALS_cp1:GmeInterchangeFormat .
    }""")

for (subject,) in HEADER_ARRAY:
    objective = rdflib.Literal(ARGS.sessionId)
    G.set((subject, IMN.hasSessionIdentifier, objective))

METRIC_ARRAY = G.query(
    """SELECT DISTINCT ?metric_name ?spec_text
       WHERE {
          ?mission_spec a IMMoRTALS_cp1:MissionSpec .
          ?mission_spec IMMoRTALS:hasHumanReadableForm ?spec_text .
          ?mission_spec IMMoRTALS:hasRightValue ?metric_name .
          ?metric_name a IMMoRTALS_metrics:Metric .
       }""")

print("graph has {} metric specs.".format(len(METRIC_ARRAY)))
for (subject, text) in METRIC_ARRAY:

    # the technique used is to
    # update/set the tuple value.

    ratio = similar(
        text, 'the software must support at least 25 concurrent clients')
    if ratio > 0.90:
        predicate = IMN.hasValue
        objective = rdflib.Literal(ARGS.clientDeviceCount)
        # print("Client Count Metric: {} {} {}".format(subject, predicate,
        # objective))
        G.set((subject, predicate, objective))
        continue

    ratio = similar(text, 'client must issue > 10 PLI messages per minute')
    if ratio > 0.90:
        predicate = IMN.hasValue
        objective = rdflib.Literal(ARGS.pliClientMsgRate)
        # print ("Client PLI Metric: {} {} {}".format(subject, predicate,
        # objective))
        G.set((subject, predicate, objective))
        continue

    ratio = similar(
        text, 'software must provide at least 1 image updates per minute')
    if ratio > 0.90:
        predicate = IMN.hasValue
        objective = rdflib.Literal(ARGS.imageClientMsgRate)
        # print ("Client Image Metric: {} {} {}".format(subject, predicate,
        # objective))
        G.set((subject, predicate, objective))
        continue

    ratio = similar(text, 'link must never see > 25kbps in traffic')
    if ratio > 0.90:
        predicate = IMN.hasValue
        objective = rdflib.Literal(ARGS.serverBandwidth)
        # print ("Client Count Metric: {} {} {}".format(subject, predicate,
        # objective))
        G.set((subject, predicate, objective))
        continue

    print("could not find match for metric {}".format(text))


# the technique used for the remainder is to
# eliminate or retain the tuple


def edit_tuple(action, res_name, predicate1, q_tuple, select):
    '''
    perfrom the edit on the tuple
    '''
    print("graph has {} resource specs.".format(res_name))
    for (subject1, objective1) in q_tuple:
        select.get(action, keep_tuple)(subject1, predicate1, objective1)


def edit_android_resource(action, res_name, q_tuple, select):
    '''
    edit the indicated tuple
    '''
    edit_tuple(action, res_name, IMN.hasPlatformResources,
               q_tuple, select)

# Android hw: Bluetooth
edit_android_resource(
    ARGS.androidBluetootRes, 'blue-tooth',
    G.query(
        """SELECT DISTINCT ?android_platform ?resource
            WHERE {
                ?android_platform a IMMoRTALS_android:AndroidPlatform .
                ?android_platform IMMoRTALS:hasPlatformResources ?resource .
                ?resource a IMMoRTALS_resources:BluetoothResource .
            }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'absent': remove_tuple,
        'active': keep_tuple,
        'bt-le': keep_tuple,
    })

# Android hw: USB
edit_android_resource(
    ARGS.androidUsbRes, 'USB',
    G.query(
        """SELECT DISTINCT ?android_platform ?resource
           WHERE {
              ?android_platform a IMMoRTALS_android:AndroidPlatform .
              ?android_platform IMMoRTALS:hasPlatformResources ?resource .
              ?resource a IMMoRTALS_resources:UsbResource .
           }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'absent': remove_tuple,
        'active': keep_tuple,
        'otg': keep_tuple,
    })

# Android hw: embedded GPS
edit_android_resource(
    ARGS.androidInternalGpsRes, 'embedded GPS',
    G.query(
        """SELECT DISTINCT ?android_platform ?resource
           WHERE {
              ?android_platform a IMMoRTALS_android:AndroidPlatform .
              ?android_platform IMMoRTALS:hasPlatformResources ?resource .
              ?resource a IMMoRTALS_resources_gps:GpsReceiverEmbedded .
           }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'none': remove_tuple,
        'single': keep_tuple,
        'multi': keep_tuple,
    })

# Android hw: user interface
edit_android_resource(
    ARGS.androidUiRes, 'User Interface',
    G.query(
        """SELECT DISTINCT ?android_platform ?resource
           WHERE {
              ?android_platform a IMMoRTALS_android:AndroidPlatform .
              ?android_platform IMMoRTALS:hasPlatformResources ?resource .
              ?resource a IMMoRTALS_resources:UserInterface .
           }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'none': remove_tuple,
        'single': keep_tuple,
        'multi': keep_tuple,
    })


# GPS satellite constellation availability
edit_tuple(
    ARGS.gpsSatelliteRes, 'GPS satellite constellation',
    IMN.hasAvailableResources,
    G.query(
        """SELECT DISTINCT ?top ?res
           WHERE {
              ?top a IMMoRTALS_cp1:GmeInterchangeFormat .
              ?top IMMoRTALS:hasAvailableResources ?res .
              ?res a IMMoRTALS_resources_gps:GpsSatelliteConstellation .
           }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'unreachable': remove_tuple,
        'reachable': keep_tuple,
        'reliable': keep_tuple,
    })


# Mission REQ: trusted communications
edit_tuple(
    ARGS.missionTrustComms, 'trusted communications',
    IMN.hasPropertyConstraint,
    G.query(
        """SELECT DISTINCT ?func_spec ?property
           WHERE {
              ?func_spec a IMMoRTALS_cp1:FunctionalitySpec .
              ?func_spec IMMoRTALS:hasPropertyConstraint ?property .
              ?property a IMMoRTALS_property_impact:PropertyConstraint .
              ?property IMMoRTALS:hasConstrainedProperty ?trusted .
              ?trusted a IMMoRTALS_resources_gps_properties:TrustedProperty .
           }"""),
    {
        'yes': keep_tuple,
        'no': remove_tuple,
        'ignore': remove_tuple,
        'use': keep_tuple,
        'require': keep_tuple,
    })


# Now we write the result.
STREAM = G.serialize(format='n3')
if ARGS.outputUrl:
    FD = open(ARGS.outputUrl, 'wb')
    FD.write(STREAM)
    FD.close()
else:
    print("{}".format(STREAM))
