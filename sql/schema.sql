CREATE TABLE user_account
(
    id              BIGINT      not null AUTO_INCREMENT comment '회원 계정 식별자',
    name            VARCHAR(31) not null comment '회원 이름',
    email           VARCHAR(63) not null comment '회원 이름',
    phone_number    VARCHAR(31) not null comment '회원 이름',
    user_type       ENUM('INSTRUCTOR', 'STUDENT') not null comment '회원 유형',
    password_digest VARCHAR(63) not null comment '비밀번호 digest',

    PRIMARY KEY (`id`),
    INDEX           ix_email(email)
) comment '회원 계정';

CREATE TABLE course
(
    id               BIGINT      not null AUTO_INCREMENT comment '강의 식별자',
    title            VARCHAR(31) not null comment '강의명',
    max_participants BIGINT      not null comment '최대 수강 인원',
    price            BIGINT      not null comment '가격',
    created_at       datetime(6) not null comment '강의 등록일',

    PRIMARY KEY (`id`)
) comment '강의';

CREATE TABLE course_registration
(
    id              BIGINT not null AUTO_INCREMENT comment '수강 신청 식별자',
    user_account_id BIGINT not null comment '회원 계정 식별자',
    course_id       BIGINT not null comment '강의 식별자',
    created_at      datetime(6) not null comment '강의 신청일',

    PRIMARY KEY (`id`)
) comment '수강 신청';
