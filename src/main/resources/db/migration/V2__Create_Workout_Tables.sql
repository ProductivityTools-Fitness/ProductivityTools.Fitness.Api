-- 1. Fitness Users table
CREATE TABLE fitness_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    default_rest_timer_seconds INTEGER DEFAULT 90,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 2. Exercises catalogue table (global system-defined read-only exercises and custom user-created exercises)
CREATE TABLE exercise (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES fitness_user(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50),
    primary_muscle VARCHAR(50),
    secondary_muscles VARCHAR(200),
    icon_url VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_exercise_owner CHECK (
        (is_system = TRUE AND user_id IS NULL) OR 
        (is_system = FALSE AND user_id IS NOT NULL)
    )
);

-- Ensure exercise name is unique per user (and unique among system exercises)
CREATE UNIQUE INDEX idx_exercise_unique_per_user ON exercise(COALESCE(user_id, 0), LOWER(name));

-- 3. Workouts sessions table
CREATE TABLE workout (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES fitness_user(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL DEFAULT 'Log Workout',
    start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ,
    duration_seconds INTEGER DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 4. Exercises performed within a specific workout session
CREATE TABLE workout_exercise (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL REFERENCES workout(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercise(id),
    order_index INTEGER NOT NULL DEFAULT 1,
    notes TEXT,
    rest_timer_seconds INTEGER,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 5. Individual sets within a workout exercise
CREATE TABLE workout_set (
    id BIGSERIAL PRIMARY KEY,
    workout_exercise_id BIGINT NOT NULL REFERENCES workout_exercise(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    set_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    weight_kg NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    reps INTEGER NOT NULL DEFAULT 0,
    rpe NUMERIC(3, 1),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 6. Workout routines and templates
CREATE TABLE workout_template (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES fitness_user(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 7. Exercises defined inside a workout template
CREATE TABLE workout_template_exercise (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES workout_template(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercise(id),
    order_index INTEGER NOT NULL DEFAULT 1,
    target_sets INTEGER NOT NULL DEFAULT 3,
    target_reps INTEGER NOT NULL DEFAULT 10,
    target_rest_seconds INTEGER DEFAULT 90
);

-- Indexes for performance and multi-user querying
CREATE INDEX idx_workout_user_id ON workout(user_id, start_time DESC);
CREATE INDEX idx_workout_exercise_workout ON workout_exercise(workout_id);
CREATE INDEX idx_workout_set_exercise ON workout_set(workout_exercise_id);
CREATE INDEX idx_exercise_user ON exercise(user_id);
