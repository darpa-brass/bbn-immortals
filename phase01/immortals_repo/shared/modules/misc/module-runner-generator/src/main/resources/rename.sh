#!/usr/bin/env bash

for i in *;do
  mv "$i" "$(echo "$i" | tr -d "'")"
#  echo $i | tr -d "'"


done
