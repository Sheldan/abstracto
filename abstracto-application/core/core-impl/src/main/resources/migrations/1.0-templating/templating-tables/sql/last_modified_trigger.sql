CREATE OR REPLACE FUNCTION last_modified_trigger_procedure() RETURNS trigger
LANGUAGE plpgsql
AS $$
    BEGIN
    NEW.last_modified := CURRENT_TIMESTAMP;
    RETURN NEW;
    END;
$$;