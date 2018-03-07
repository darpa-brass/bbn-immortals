#!/bin/bash



# create schema
sqlcmd < /ddl-master-source.sql
sqlcmd < /ddl-master-cot-event.sql

# load database

csvloader --separator "," --skip 1 --file /source.csv source -r ./log
csvloader --separator "," --skip 1 --file /master_cot_event.csv master_cot_event -r ./log
