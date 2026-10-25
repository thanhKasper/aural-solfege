INSERT INTO exercises (id, owner_id, training_methodology, title, description, repetitions, rest, exercise_activities) VALUES ('48cdbd72-83ca-4be4-8292-6fc1db16b10a', '11111111-1111-4111-8111-111111111111', 'INTERVAL_TRAINING', 'Basic Interval Training', 'Practice identifying major 2nd and perfect 5th intervals in ascending motion.', 2, 100, '[{"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["MAJOR_2ND"], "position": 0}, {"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["PERFECT_5TH"], "position": 1}]');

INSERT INTO exercises (id, owner_id, training_methodology, title, description, repetitions, rest, exercise_activities) VALUES ('2f3ae143-610c-474c-bb59-db4b4d3c08ba', '11111111-1111-4111-8111-111111111111', 'INTERVAL_TRAINING', 'Interval Sound Comparison', 'Practice comparing whether the second interval is higher or lower than the first interval.', 2, 100, '[{"type": "INTERVAL_SOUND_COMPARISON", "texture": "ASCENDING", "firstInterval": "PERFECT_4TH", "secondInterval": "PERFECT_5TH", "position": 0}]');

INSERT INTO instruments (id, instrument_type, lowest_pitch, highest_pitch) values ('61457859-e70c-4a5f-b041-ae4e7171c6ab', 'PIANO', 'A0', 'C8');

INSERT INTO musical_config (id, active_instrument_id) SELECT gen_random_uuid(), i.id FROM instruments i WHERE i.instrument_type = 'PIANO';
-- One IN_PROGRESS session per learner and exercise (partial index: not expressible in JPA)
CREATE UNIQUE INDEX uq_active_session ON practice_sessions (learner_id, exercise_id) WHERE status = 'IN_PROGRESS';
