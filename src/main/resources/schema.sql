create table if not exists exercise
(
    id                   UUID,
    title                nvarchar(256) not null,
    description          nvarchar(3000),
    training_methodology TEXT          not null,
    repetitions          INT,

    constraint pk_exercise PRIMARY KEY (id),
    constraint positive_repetition CHECK (repetitions > 0)
);

create table if not exists passive_training
(
    id                   UUID,
    musical_interval     VARCHAR(20)  not null,
    sound_texture        VARCHAR(10) not null,
    training_methodology TEXT        not null,
    exercise_id          UUID,

    constraint pk_passive_training PRIMARY KEY (id),
    constraint fk_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id)
);