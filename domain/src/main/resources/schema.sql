drop table if exists user;
drop table if exists blog;
drop table if exists article;

create table user
(
    id           bigint auto_increment primary key,
    email        varchar(200) not null comment '이메일 주소',
    password     varchar(200) null comment '암호화된 비밀번호',
    receive_days varchar(100) not null comment '수신 희망 요일',
    created_at   datetime     not null comment '생성 시각',
    unique index udx_email (email)
) comment '사용자';

create table blog
(
    id         bigint auto_increment primary key,
    name       varchar(200) null comment '이름',
    url        varchar(200) null comment 'URL',
    created_by bigint       not null comment '생성자 user.id',
    created_at datetime     not null comment '생성 시각'
) comment '블로그';

create table subscription
(
    id         bigint auto_increment primary key,
    user_id    bigint   not null comment 'user.id',
    blog_id    bigint   not null comment 'blog.id',
    created_at datetime not null comment '구독 시각'
) comment '사용자-블로그 구독 정보';

create table article
(
    id            bigint auto_increment primary key,
    blog_id       bigint        not null comment '블로그 id',
    title         varchar(200)  not null comment '제목',
    summary       varchar(1000) not null comment '요약된 내용',
    summarized_by varchar(100)  not null comment '요약된 내용 제공자',
    url           varchar(200)  not null comment 'URL',
    created_date  date          not null comment '생성 날짜'
) comment '블로그 글';