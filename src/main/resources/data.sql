INSERT INTO training_methodology
values (1,
        'INTERVAL_TRAINING');

SET @target_id = random_uuid();

INSERT INTO training_methodology
values (2,
        'FUNCTIONAL_EAR_TRAINING');

INSERT INTO exercise
values (@target_id,
        'Practice interval perfect 5th',
        null,
        1,
        2);

INSERT INTO passive_training
values (random_uuid(),
        'P5',
        'ASCENDING',
        @target_id);