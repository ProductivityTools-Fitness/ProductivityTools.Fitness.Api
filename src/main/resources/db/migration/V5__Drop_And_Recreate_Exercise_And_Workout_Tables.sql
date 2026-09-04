-- Drop dependent workout tables and exercise table
DROP TABLE IF EXISTS workout_set CASCADE;
DROP TABLE IF EXISTS workout_exercise CASCADE;
DROP TABLE IF EXISTS workout_template_exercise CASCADE;
DROP TABLE IF EXISTS workout_template CASCADE;
DROP TABLE IF EXISTS workout CASCADE;
DROP TABLE IF EXISTS exercise CASCADE;

-- 1. Recreate exercise table (matching ExerciseDB format, without redundant columns)
CREATE TABLE exercise (
    id BIGSERIAL PRIMARY KEY,
    external_exercise_id VARCHAR(50),
    user_id BIGINT REFERENCES fitness_user(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    gif_url VARCHAR(500),
    equipment_category VARCHAR(50),
    body_category VARCHAR(50),
    target_muscle VARCHAR(100),
    secondary_muscles JSONB NOT NULL DEFAULT '[]'::jsonb,
    instructions JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_exercise_owner CHECK (
        (is_system = TRUE AND user_id IS NULL) OR 
        (is_system = FALSE AND user_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX idx_exercise_unique_per_user ON exercise(COALESCE(user_id, 0), LOWER(name));
CREATE UNIQUE INDEX idx_exercise_external_exercise_id ON exercise(external_exercise_id) WHERE external_exercise_id IS NOT NULL;
CREATE INDEX idx_exercise_body_category ON exercise(body_category);
CREATE INDEX idx_exercise_equipment_category ON exercise(equipment_category);
CREATE INDEX idx_exercise_target_muscle ON exercise(target_muscle);
CREATE INDEX idx_exercise_secondary_muscles_gin ON exercise USING GIN (secondary_muscles);
CREATE INDEX idx_exercise_user ON exercise(user_id);

-- 2. Recreate workout table
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

-- 3. Recreate workout_exercise table
CREATE TABLE workout_exercise (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL REFERENCES workout(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercise(id),
    order_index INTEGER NOT NULL DEFAULT 1,
    notes TEXT,
    rest_timer_seconds INTEGER,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 4. Recreate workout_set table
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

-- 5. Recreate workout_template table
CREATE TABLE workout_template (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES fitness_user(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 6. Recreate workout_template_exercise table
CREATE TABLE workout_template_exercise (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES workout_template(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercise(id),
    order_index INTEGER NOT NULL DEFAULT 1,
    target_sets INTEGER NOT NULL DEFAULT 3,
    target_reps INTEGER NOT NULL DEFAULT 10,
    target_rest_seconds INTEGER DEFAULT 90
);

-- Indexes for performance and querying
CREATE INDEX idx_workout_user_id ON workout(user_id, start_time DESC);
CREATE INDEX idx_workout_exercise_workout ON workout_exercise(workout_id);
CREATE INDEX idx_workout_set_exercise ON workout_set(workout_exercise_id);
