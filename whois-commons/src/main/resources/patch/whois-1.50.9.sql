-- Add index on history for timestamp to speedup unref cleanup

CREATE INDEX history_timestamp on history ( timestamp );
