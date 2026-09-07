ALTER TABLE workout_set ADD COLUMN IF NOT EXISTS prev_weight_kg NUMERIC(6, 2);
ALTER TABLE workout_set ADD COLUMN IF NOT EXISTS prev_reps INTEGER;
CREATE INDEX IF NOT EXISTS idx_workout_exercise_exercise_id ON workout_exercise(exercise_id);
