#!/usr/bin/env bash

mkdir saxon
wget https://downloads.sourceforge.net/project/saxon/Saxon-HE/9.9/SaxonHE9-9-1-5J.zip -O saxon/saxon.zip
unzip -d saxon saxon/saxon.zip
rm saxon/saxon.zip

