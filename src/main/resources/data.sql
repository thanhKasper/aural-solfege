SET @target_id = random_uuid();

INSERT INTO exercise (id, title, description, training_methodology, repetitions)
values (@target_id, 'Practice interval perfect 5th', null, 'INTERVAL_TRAINING', 2);

INSERT INTO passive_training (id, musical_interval, training_methodology, sound_texture, exercise_id)
values (DEFAULT, 'PERFECT_5TH',
        'INTERVAL_TRAINING',
        'ASCENDING',
        @target_id);