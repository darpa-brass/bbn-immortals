FROM android_docker

# Get Required libraries
RUN apt-get update -y && \
    apt-get install -y time unzip

# Extract the static analysis framework
ADD bbnAnalysis.tar.gz /

# Remove the previously existing artifacts
RUN rm -r /bbnAnalysis/apks/*
RUN rm -r /bbnAnalysis/output/*

# Strip that ampersand so it won't return right away
RUN sed 's/&$//' /bbnAnalysis/api-graph.sh > /bbnAnalysis/api-graph.sh.tmp && mv /bbnAnalysis/api-graph.sh.tmp /bbnAnalysis/api-graph.sh
