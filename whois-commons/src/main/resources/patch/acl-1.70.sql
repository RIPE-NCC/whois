--
-- Normalise all IPv6 prefixes in the ACL database
--
-- Issue fixed in v1.70 release
--

--
-- begin a new transaction
--

SET autocommit = 0;
START TRANSACTION;

--
-- begin patch
--

use ACL_RIPE;

update acl_denied set prefix = replace(prefix, ':0:0:0:0:0:0/64', '::/64');
update acl_denied set prefix = replace(prefix, ':0:0:0:0:0/64', '::/64');
update acl_denied set prefix = replace(prefix, ':0:0:0:0/64', '::/64');

update acl_event set prefix = replace(prefix, ':0:0:0:0:0:0/64', '::/64');
update acl_event set prefix = replace(prefix, ':0:0:0:0:0/64', '::/64');
update acl_event set prefix = replace(prefix, ':0:0:0:0/64', '::/64');

update acl_limit set prefix = replace(prefix, ':0:0:0:0:0:0/64', '::/64');
update acl_limit set prefix = replace(prefix, ':0:0:0:0:0/64', '::/64');
update acl_limit set prefix = replace(prefix, ':0:0:0:0/64', '::/64');
update acl_limit set prefix = replace(prefix, ':0:0:0:0:0:0/48', '::/48');
update acl_limit set prefix = replace(prefix, ':0:0:0:0:0/48', '::/48');
update acl_limit set prefix = replace(prefix, ':0:0:0:0/48', '::/48');

update acl_proxy set prefix = replace(prefix, ':0:0:0:0:0:0/64', '::/64');
update acl_proxy set prefix = replace(prefix, ':0:0:0:0:0/64', '::/64');
update acl_proxy set prefix = replace(prefix, ':0:0:0:0/64', '::/64');
update acl_proxy set prefix = replace(prefix, ':0:0:0:0:0:0/48', '::/48');
update acl_proxy set prefix = replace(prefix, ':0:0:0:0:0/48', '::/48');
update acl_proxy set prefix = replace(prefix, ':0:0:0:0/48', '::/48');

INSERT INTO version VALUES ('acl-1.70');

--
-- commit the transaction
--

COMMIT;
SET autocommit = 1;
