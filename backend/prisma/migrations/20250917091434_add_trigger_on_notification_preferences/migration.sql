CREATE OR REPLACE FUNCTION add_default_notification_preferences()
RETURNS TRIGGER AS $$
BEGIN
    /* Check if new user has USER role*/
    IF NEW."role" = 'USER' THEN
        INSERT INTO "user_notification_preferences" ("userId", "category")
        SELECT NEW."userId", unnest(enum_range(NULL::"NotificationCategory"));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_notification_preferences_trigger
AFTER INSERT ON "users"
FOR EACH ROW
EXECUTE FUNCTION add_default_notification_preferences();