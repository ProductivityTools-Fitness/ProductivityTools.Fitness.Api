CREATE TABLE IF NOT EXISTS workout_schedule (
    id SERIAL PRIMARY KEY,
    day_number INT NOT NULL CHECK (day_number BETWEEN 1 AND 7),
    day_name VARCHAR(15) NOT NULL,
    main_activity VARCHAR(255) NOT NULL,
    goal_benefits TEXT,
    pull_up_protocol TEXT,
    suggested_time TEXT
);

