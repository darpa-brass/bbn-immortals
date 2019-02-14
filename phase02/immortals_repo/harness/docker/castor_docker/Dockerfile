FROM voltdb_docker

ENV VOLTDB_HOME /immortals/osu/voltdb
ENV PATH "${PATH}:${VOLTDB_HOME}/bin"

COPY entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
