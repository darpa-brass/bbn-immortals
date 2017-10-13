#!/bin/sh

warning="
IMPORTANT: Running this file will create query training data files (positive and negative) in the current directory. If a file with the same name already exists, it will be deleted.

You can check in the resulting files to the appropriate location in the Immortals repo (e.g., castor folder). This script assumes the baseline schema in the local Immortals database is populated.

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
baseline_schema="baseline"
target_dir_for_training_files="`pwd`"
################################################################################################################################
number_queries=9
pos_limit=5000
neg_limit=10000
################################################################################################################################

#Positive and negative queries are defined below
################################################################################################################################
posQuery1="select id, source_id, cot_type
from baseline.cot_event
where cot_type = 'a-n-A-C-F-s'
order by random()
limit $pos_limit"

negQuery1="select id, source_id, cot_type
from baseline.cot_event
where cot_type <> 'a-n-A-C-F-s'
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery2="select id, source_id, cot_type, how
from baseline.cot_event
where servertime = 201705071635
order by random()
limit $pos_limit"

negQuery2="select id, source_id, cot_type, how
from baseline.cot_event
where servertime <> 201705071635
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery3="select id, source_id, cot_type, how
from baseline.cot_event
where servertime = 201705071635 
and cot_type = 'a-n-A-C-F-m'
order by random()
limit $pos_limit"

negQuery3="select id, source_id, cot_type, how
from baseline.cot_event
where not (servertime = 201705071635 
and cot_type = 'a-n-A-C-F-m')
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery4="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where channel = 7
order by random()
limit $pos_limit"

negQuery4="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where channel <> 7
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery5="select ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where channel = 7
order by random()
limit $pos_limit"

negQuery5="select ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where channel <> 7
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery6="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where s.channel = 5 or ce.cot_type = 'a-n-A-C-F-s'
order by random()
limit $pos_limit"

negQuery6="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
where not (s.channel = 5 or ce.cot_type = 'a-n-A-C-F-s')
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery7="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
join baseline.cot_event_position cep on ce.id = cep.cot_event_id
where  s.channel = 6 and tilex = 18830 and tiley = 25704
order by random()
limit $pos_limit"

negQuery7="select s.name, ce.id, ce.cot_type, ce.servertime
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
join baseline.cot_event_position cep on ce.id = cep.cot_event_id
where  not (s.channel = 6 and tilex = 18830 and tiley = 25704)
order by random()
limit $neg_limit"
################################################################################################################################



################################################################################################################################
posQuery8="with sampleSizes as	
(	
select source_id, servertime, min(sample_size) as sample_size	
from	
(select s.id source_id, ce.servertime, 	
ceil((cast(count(*) over(partition by s.id, ce.servertime) as float) / cast( count(*) over() as float)) * 5000) as sample_size	
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id) as t1	
group by t1.source_id, t1.servertime	
),	
	
samples as	
(	
select source_id, source_name, ce_id, servertime, row_number() over(partition by source_name, servertime) as rownum, tilex, tiley	
from	
(select s.id as source_id, s.name as source_name, ce.id as ce_id, ce.servertime, cep.tilex, cep.tiley,	
row_number() over(order by s.name, ce.servertime, random()) as rownum	
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id	
join baseline.cot_event_position cep on ce.id = cep.cot_event_id	
join (	select t1.name, t1.servertime
	from (select distinct s2.name, ce2.servertime
	      from baseline.source s2 join baseline.cot_event ce2 on s2.id = ce2.source_id
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
posQuery9="select s.id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
join baseline.cot_event_position cep on ce.id = cep.cot_event_id
where s.name = 'ABD19E' and servertime = 201705071645
limit $pos_limit"

negQuery9="select s.id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley
from baseline.source s join baseline.cot_event ce on s.id = ce.source_id
join baseline.cot_event_position cep on ce.id = cep.cot_event_id
where not (s.name = 'ABD19E' and servertime = 201705071645)
order by random()
limit $neg_limit"
################################################################################################################################


for ((i=1; i<=$number_queries;i++));
do
    posQname="posQuery$i"
    negQname="negQuery$i"

    eval "pos_sql=\$$posQname"
    eval "neg_sql=\$$negQname"
        
    posTarget="$target_dir_for_training_files/query$i""_all_pos.csv"
    negTarget="$target_dir_for_training_files/query$i""_all_neg.csv"
    
    echo "Processing query: $posQname ..."
	command="COPY ($pos_sql) to '$posTarget' WITH (FORMAT CSV, DELIMITER ',', HEADER TRUE, QUOTE '\"')"
	psql -a -d $immortals_db -c "$command"

    echo "Processing query: $negQname ..."
	command="COPY ($neg_sql) to '$negTarget' WITH (FORMAT CSV, DELIMITER ',', HEADER TRUE, QUOTE '\"')"
	psql -a -d $immortals_db -c "$command"
done
 
