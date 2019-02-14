#!/bin/bash

# create schema
sqlcmd < ddl-schema-baseline.sql

# load database

csvloader --separator "," --skip 1 --file source.csv source
csvloader --separator "," --skip 1 --file cot_event.csv cot_event
csvloader --separator "," --skip 1 --file cot_event_position.csv cot_event_position
