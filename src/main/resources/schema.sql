create table if not exists training_methodology
(
    id          int primary key,
    methodology VARCHAR(50) not null
);

create table if not exists exercise
(
    id                   UUID,
    title                nvarchar(256) not null,
    description          nvarchar(3000),
    training_methodology INT           not null,
    repetition           int,

    constraint pk_exercise PRIMARY KEY (id),
    constraint fk_learning_methodology FOREIGN KEY (training_methodology)
        REFERENCES training_methodology (id),
    constraint positive_repetition CHECK (repetition > 0)
);

create table if not exists passive_training
(
    id               UUID,
    musical_interval VARCHAR(2)  not null,
    sound_texture    VARCHAR(10) not null,
    exercise_id      UUID,

    constraint pk_passive_training PRIMARY KEY (id),
    constraint fk_exercise FOREIGN KEY (exercise_id) REFERENCES exercise (id)
);