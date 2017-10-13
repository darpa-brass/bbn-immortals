FROM voltdb_docker

ENV VOLTDB_HOME /immortals/osu/voltdb
ENV PATH "${PATH}:${VOLTDB_HOME}/bin"

COPY entrypoint.sh /
COPY cot_event_position.csv /
COPY cot_event.csv /
COPY source.csv /
COPY castor_schema_ddl.sql /

ENTRYPOINT ["/entrypoint.sh"]
