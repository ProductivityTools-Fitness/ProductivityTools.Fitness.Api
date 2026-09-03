-- Insert a default user for local testing and unauthenticated workout logging
INSERT INTO fitness_user (email, username, default_rest_timer_seconds)
VALUES ('default@fitness.top', 'Default User', 90)
ON CONFLICT (email) DO NOTHING;
