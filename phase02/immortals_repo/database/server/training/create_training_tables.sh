#!/bin/sh

set -e

#NOTE: You don't need to run this file. It was used initially to provide OSU/Castor team
#data to evaluate queries. In the actual DAS system, all training data is created dynamically
#when the DAS is started in analyze mode.

warning="NOTE: Running this file is not needed for the DAS system.

This file will create query training tables in the das database.

This script assumes the takrptbase and das schema create already created.

Run as follows: sudo -u postgres ../create_training_tables.sh

Continue ('yes' or 'no')?: "

while true; do
	read -p "$warning" yn
	case $yn in
		[Yy]* ) break;;
		[Nn]* ) exit;;
		* ) echo "Type yes or no.";;	
	esac
done

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
das_schema="das"
################################################################################################################################
number_queries=1
pos_limit=5000
neg_limit=10000
################################################################################################################################

#Positive and negative queries are defined below
################################################################################################################################
posQuery1="select id, source_id, cot_type
from takrptbase.cot_event
where cot_type = 'a-n-A-C-F-s'
order by random()
limit $pos_limit"

negQuery1="select id, source_id, cot_type
from takrptbase.cot_event
where cot_type <> 'a-n-A-C-F-s'
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery2="select id, source_id, cot_type, how
from takrptbase.cot_event
where servertime = 201705071635
order by random()
limit $pos_limit"

negQuery2="select id, source_id, cot_type, how
from takrptbase.cot_event
where servertime <> 201705071635
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery3="select id, source_id, cot_type, how
from takrptbase.cot_event
where servertime = 201705071635 
and cot_type = 'a-n-A-C-F-m'
order by random()
limit $pos_limit"

negQuery3="select id, source_id, cot_type, how
from takrptbase.cot_event
where not (servertime = 201705071635 
and cot_type = 'a-n-A-C-F-m')
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery4="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where channel = 7
order by random()
limit $pos_limit"

negQuery4="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where channel <> 7
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery5="select ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where channel = 7
order by random()
limit $pos_limit"

negQuery5="select ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where channel <> 7
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery6="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where s.channel = 5 or ce.cot_type = 'a-n-A-C-F-s'
order by random()
limit $pos_limit"

negQuery6="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
where not (s.channel = 5 or ce.cot_type = 'a-n-A-C-F-s')
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery7="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
join takrptbase.cot_event_position cep on ce.id = cep.cot_event_id
where  s.channel = 6 and tilex = 18830 and tiley = 25704
order by random()
limit $pos_limit"

negQuery7="select s.name, ce.id, ce.cot_type, ce.servertime
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
join takrptbase.cot_event_position cep on ce.id = cep.cot_event_id
where  not (s.channel = 6 and tilex = 18830 and tiley = 25704)
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery8="with sampleSizes as	
(	
select source_id, servertime, min(sample_size) as sample_size	
from	
(select s.source_id source_id, ce.servertime, 	
ceil((cast(count(*) over(partition by s.source_id, ce.servertime) as float) / cast( count(*) over() as float)) * 5000) as sample_size	
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id) as t1	
group by t1.source_id, t1.servertime	
),	
	
samples as	
(	
select source_id, source_name, ce_id, servertime, row_number() over(partition by source_name, servertime) as rownum, tilex, tiley	
from	
(select s.source_id as source_id, s.name as source_name, ce.id as ce_id, ce.servertime, cep.tilex, cep.tiley,	
row_number() over(order by s.name, ce.servertime, random()) as rownum	
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id	
join takrptbase.cot_event_position cep on ce.id = cep.cot_event_id	
join (	select t1.name, t1.servertime
	from (select distinct s2.name, ce2.servertime
	      from takrptbase.source s2 join takrptbase.cot_event ce2 on s2.source_id = ce2.source_id
	     ) t1
	order by random()
	limit 50) as t2 on s.name = t2.name and ce.servertime = t2.servertime
) as t3 	
)	
	
select samples.source_id, samples.source_name, samples.ce_id, samples.servertime, samples.tilex, samples.tiley	
from samples join sampleSizes on samples.source_id = sampleSizes.source_id and samples.servertime = sampleSizes.servertime	
where samples.rownum <= sampleSizes.sample_size"

negQuery8="select 'none' as c1"
################################################################################################################################


################################################################################################################################
posQuery9="select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
join takrptbase.cot_event_position cep on ce.id = cep.cot_event_id
where s.name = 'ABD19E' and servertime = 201705071645
limit $pos_limit"

negQuery9="select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley
from takrptbase.source s join takrptbase.cot_event ce on s.source_id = ce.source_id
join takrptbase.cot_event_position cep on ce.id = cep.cot_event_id
where not (s.name = 'ABD19E' and servertime = 201705071645)
order by random()
limit $neg_limit"
################################################################################################################################


i=1
while [ "$i" -le $number_queries ]; do
    posQname="posQuery$i"
    negQname="negQuery$i"

    eval "pos_sql=\$$posQname"
    eval "neg_sql=\$$negQname"

    echo "Processing query: $posQname ..."
    command="CREATE TABLE das.$posQname AS ($pos_sql) "
	psql -a -d $immortals_db -c "$command"

    echo "Processing query: $negQname ..."
    command="CREATE TABLE das.$negQname AS ($neg_sql) "
	psql -a -d $immortals_db -c "$command"
	i=$(( i + 1 ))
done
 
