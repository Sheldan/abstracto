CREATE OR REPLACE FUNCTION insert_trigger_procedure() RETURNS trigger
LANGUAGE plpgsql
AS $$
    BEGIN
    NEW.created := CURRENT_TIMESTAMP;
    RETURN NEW;
    END;
$$;