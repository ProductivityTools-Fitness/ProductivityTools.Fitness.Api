-- Insert standard built-in system exercises (read-only for all users)
INSERT INTO exercise (user_id, name, category, primary_muscle, secondary_muscles, is_system) VALUES
(NULL, 'Deadlift (Barbell)', 'BARBELL', 'BACK', 'LEGS, GLUTES, FOREARMS', TRUE),
(NULL, 'Bent Over Row (Barbell)', 'BARBELL', 'BACK', 'BICEPS, SHOULDERS', TRUE),
(NULL, 'Bench Press (Barbell)', 'BARBELL', 'CHEST', 'TRICEPS, SHOULDERS', TRUE),
(NULL, 'Incline Dumbbell Press', 'DUMBBELL', 'CHEST', 'SHOULDERS, TRICEPS', TRUE),
(NULL, 'Squat (Barbell)', 'BARBELL', 'LEGS', 'GLUTES, CORE', TRUE),
(NULL, 'Leg Press', 'MACHINE', 'LEGS', 'GLUTES', TRUE),
(NULL, 'Overhead Press (Barbell)', 'BARBELL', 'SHOULDERS', 'TRICEPS', TRUE),
(NULL, 'Lateral Raise (Dumbbell)', 'DUMBBELL', 'SHOULDERS', 'TRAPS', TRUE),
(NULL, 'Pull Up', 'BODYWEIGHT', 'BACK', 'BICEPS', TRUE),
(NULL, 'Bicep Curl (Dumbbell)', 'DUMBBELL', 'ARMS', 'FOREARMS', TRUE),
(NULL, 'Tricep Rope Pushdown', 'CABLE', 'ARMS', 'CHEST', TRUE),
(NULL, 'Lat Pulldown (Cable)', 'CABLE', 'BACK', 'BICEPS', TRUE)
ON CONFLICT DO NOTHING;
