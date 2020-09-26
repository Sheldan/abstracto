CREATE OR REPLACE FUNCTION next_counter(p_counter_key VARCHAR(255), p_server_id BIGINT)
RETURNS bigint as $new_count$
DECLARE v_next_count bigint;
v_exists int;
BEGIN
    SELECT count(1)
    FROM COUNTER
    INTO v_exists
    WHERE server_id = p_server_id
    AND counter_key = p_counter_key;

    IF v_exists >= 1 THEN
        SELECT MAX(counter) + 1
        INTO v_next_count
        FROM counter
        WHERE server_id = p_server_id
        AND counter_key = p_counter_key;

        UPDATE counter
        SET counter = v_next_count
        WHERE server_id = p_server_id
        AND counter_key = p_counter_key;
    ELSE
        v_next_count := 1;
        INSERT INTO counter (counter_key, server_id, counter)
            VALUES (p_counter_key, p_server_id, v_next_count);
    END IF;
    RETURN v_next_count;
END;
$new_count$ LANGUAGE plpgsql;