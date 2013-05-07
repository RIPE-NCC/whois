/* Migrate existing NRTM access data into new ACL DB

Input: WHOIS_QUERY_ADMIN database on dbc-whois[1234] (they are the same, thanks to nrtmadminsync)

create schema:

CREATE TABLE acl_mirror (
  prefix VARCHAR(50) NOT NULL,
  comment TEXT,
  PRIMARY KEY (prefix)
);

insert into acl_mirror select concat(inet_ntoa(prefix), '/', prefix_length), comment from aaa;

for aaa6, we don't have any external customers, and it is also not under nrtmadminsync, so just enter these two entries manually:

insert into acl_mirror values ('2001:0610:240::/64', 'RIPE NCC network (!!! /64 instead of the registered /48!!!)');
insert into acl_mirror values ('2001:067C:2E8:5::/64', '#1027820, Wolfgang Nagele, wnagele@ripe.net, RIPE NCC, In production use for DNSProv. Do not delete');

*/