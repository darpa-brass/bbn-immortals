--This sql file was used to perform initial load from cot_router (safe-to-use ads-b data)
--This data is kept in castor/takserver folder as csv files.

--You do *not* need to run this sql file on any database. The cot_router_raw table
--should hold your raw input data (in our case, we got this data via ads-b flight data
--connected to a client/tak router server.
--This folder also contains an overloaded version of the tilexy function used below, which you can also ignore.
--The version of the tilexy function used in the marti app is in the parent folder.

SET search_path TO baseline, public;

delete from cot_event_position;
delete from cot_event;
delete from source;

insert into  source (name)
select distinct uid from cot_router_raw;

insert into cot_event (id, source_id, cot_type, how, detail, servertime)
select cr.id, s.id, cr.cot_type, cr.how, cr.detail, cast (to_char(chunk_time(servertime, '5 minutes'), 'YYYYMMDDHH24MI') as bigint)
from cot_router_raw cr join source s on cr.uid = s.name;

insert into cot_event_position (cot_event_id, point_hae, point_ce, point_le, tilex, tiley, longitude, latitude)
select ce.id, round(cr.point_hae), round(cr.point_ce), round(cr.point_le), 
(tilexy(cr.event_pt, 16)).tilex,
(tilexy(cr.event_pt, 16)).tiley,
cast (cast (ST_AsGeoJSON(cr.event_pt)::json->'coordinates'-> 0 as varchar) as numeric),
cast (cast (ST_AsGeoJSON(cr.event_pt)::json->'coordinates'-> 1 as varchar) as numeric)
from cot_event ce join cot_router_raw cr on ce.id = cr.id;