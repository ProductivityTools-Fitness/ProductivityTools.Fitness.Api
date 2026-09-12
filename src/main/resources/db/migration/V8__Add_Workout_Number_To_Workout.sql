ALTER TABLE workout ADD COLUMN workout_number INTEGER;

-- Backfill existing workouts per user if any exist
WITH numbered AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY start_time ASC, id ASC) as rn
    FROM workout
)
UPDATE workout w
SET workout_number = numbered.rn
FROM numbered
WHERE w.id = numbered.id;

ALTER TABLE workout ALTER COLUMN workout_number SET NOT NULL;

CREATE INDEX idx_workout_user_workout_number ON workout(user_id, workout_number DESC);
