SET @target_id = random_uuid();

INSERT INTO exercise (id, title, description, training_methodology, repetitions)
values (@target_id, 'Practice interval perfect 5th', null, 'INTERVAL_TRAINING', 2);

INSERT INTO passive_training (id, musical_interval, training_methodology, sound_texture, exercise_id)
values (RANDOM_UUID(), 'PERFECT_5TH',
        'INTERVAL_TRAINING',
        'ASCENDING',
        @target_id);

SET @active_training_id = RANDOM_UUID();
INSERT INTO active_training (id, exercise_amounts, training_methodology, exercise_id)
VALUES (@active_training_id, 5, 'INTERVAL_TRAINING', @target_id);

INSERT INTO ACTIVE_EXERCISE_FORMAT_PRACTICE_INTERVALS (ACTIVE_EXERCISE_FORMAT_ID, PRACTICE_INTERVALS)
VALUES (@active_training_id, 'PERFECT_5TH');

INSERT INTO ACTIVE_EXERCISE_FORMAT_PRACTICE_INTERVALS (ACTIVE_EXERCISE_FORMAT_ID, PRACTICE_INTERVALS)
VALUES (@active_training_id, 'MAJOR_2ND');