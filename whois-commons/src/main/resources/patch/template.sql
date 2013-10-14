--
-- Description:       <add a description of what the patch does>
--
-- Issue:             <add GitHub issue #>
--
-- Release Version:   <version>
--

--
-- begin a new transaction
--

SET autocommit = 0;
START TRANSACTION;

--
-- begin patch
--


--
-- update version table
--
INSERT INTO version VALUES ('component-x.y');


--
-- commit the transaction
--

COMMIT;
SET autocommit = 1;
