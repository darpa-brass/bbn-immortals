#!/bin/bash
#
# Presume the ...
#     mock_mission_server.py
# ...was previously started.
#
# python3 ./py/mock_mission_server.py
#
# The actual server source code is found...
#   immortals-svn/das/das-service/src/main/
#      java/mil/darpa/immortals/core/
#          das/restendpoints/DASEndpoint.java
#
#

# curl  -i -X POST \
#  -F "file=@./template/gme-interchange-example-2.ttl" \
#  -F "filename=gmei.ttl.upload" \
#  -F "comment=This is a Senario Description File" \
#  http://localhost:8088/deployment-model

# curl -H "Content-Type:text/plain" \
#     -X POST \
#     --data-urlencode @./small-1.ttl \
#     --data-urlencode @./small-2.ttl \
#     http://localhost:8088/das/deployment-model
#

curl -H "Content-Type:text/plain" \
     -X POST \
     --data-binary @./template/gme-interchange-example-2.ttl \
     http://localhost:8088/das/deployment-model
