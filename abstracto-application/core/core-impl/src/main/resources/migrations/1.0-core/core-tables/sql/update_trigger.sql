CREATE OR REPLACE FUNCTION update_trigger_procedure() RETURNS trigger
LANGUAGE plpgsql
AS $$
    BEGIN
    NEW.updated := CURRENT_TIMESTAMP;
    RETURN NEW;
    END;
$$;