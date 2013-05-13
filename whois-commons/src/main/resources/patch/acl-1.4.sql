ALTER TABLE acl_event
  CHANGE event_type event_type VARCHAR(20) NOT NULL;

ALTER TABLE acl_event
  CHANGE event_time event_time timestamp not null default 0;

UPDATE acl_event
  SET event_type = 'BLOCK_TEMPORARY'
  WHERE event_type = 'BLOCK';

-- update comment date from mm/dd/yy to yyyy-mm-dd format
UPDATE acl_denied 
  SET comment = concat(substring_index(comment,' ',8), ' 2012-03-', substring(comment,locate('/',comment)+1,locate('/',comment,locate('/',comment)+1)-locate('/',comment)-1)) 
  WHERE comment LIKE '%3/%/12';