INSERT INTO exercises (id, training_methodology, title, description, repetitions, rest, exercise_activities) VALUES ('48cdbd72-83ca-4be4-8292-6fc1db16b10a', 'INTERVAL_TRAINING', 'Basic Interval Training', 'Practice identifying major 2nd and perfect 5th intervals in ascending motion.', 3, 5, '[{"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["MAJOR_2ND"], "position": 0}, {"type": "SINGLE_INTERVAL", "soundProperty": "ASCENDING", "intervals": ["PERFECT_5TH"], "position": 1}]');

INSERT INTO instruments (id, instrument_type, lowest_pitch, highest_pitch) values ('61457859-e70c-4a5f-b041-ae4e7171c6ab', 'PIANO', 'A0', 'C8');

INSERT INTO musical_config (id, active_instrument_id) SELECT gen_random_uuid(), i.id FROM instruments i WHERE i.instrument_type = 'PIANO';