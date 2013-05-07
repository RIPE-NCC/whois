-- Store denied date
ALTER TABLE acl_denied
ADD denied_date DATE;

UPDATE acl_denied
SET denied_date = CURRENT_DATE()
WHERE denied_date IS NULL;

ALTER TABLE acl_denied
CHANGE denied_date denied_date DATE NOT NULL DEFAULT 0;

-- Add option to allow unlimited connections from a single IP address
ALTER TABLE acl_limit
ADD unlimited_connections BIT NOT NULL DEFAULT 0;

UPDATE acl_limit
SET unlimited_connections = 1
WHERE daily_limit = -1;
