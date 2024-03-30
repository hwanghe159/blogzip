drop table if exists user;
drop table if exists blog;
drop table if exists subscription;
drop table if exists article;

create table user
(
    id           bigint auto_increment primary key,
    email        varchar(200) not null comment '이메일 주소',
    password     varchar(200) null comment '암호화된 비밀번호',
    receive_days varchar(100) not null comment '수신 희망 요일',
    created_at   datetime     not null default current_timestamp comment '생성 시각',
    unique index udx_email (email)
) comment '사용자';

create table blog
(
    id         bigint auto_increment primary key,
    name       varchar(200) not null comment '이름',
    url        varchar(200) not null comment 'URL',
    rss        varchar(200) null comment 'RSS URL',
    created_by bigint       not null comment '생성자 user.id',
    created_at datetime     not null default current_timestamp comment '생성 시각',
    unique index udx_url (url)
) comment '블로그';

create table subscription
(
    id         bigint auto_increment primary key,
    user_id    bigint   not null comment 'user.id',
    blog_id    bigint   not null comment 'blog.id',
    created_at datetime not null default current_timestamp comment '구독 시각'
) comment '사용자-블로그 구독 정보';

create table article
(
    id            bigint auto_increment primary key,
    blog_id       bigint        not null comment '블로그 id',
    title         varchar(200)  not null comment '제목',
    summary       varchar(1000) null comment '요약된 내용',
    summarized_by varchar(100)  null comment '요약된 내용 제공자',
    url           varchar(700)  not null comment 'URL',
    created_date  date          not null comment '생성 날짜',
    unique index udx_url (url)
) comment '블로그 글';

insert into user(email, password, receive_days)
values ('hwanghe159@gmail.com', null, '[]');
insert into blog (name, url, rss, created_by)
values ('우아한형제들 기술블로그', 'https://techblog.woowahan.com', 'https://techblog.woowahan.com/feed/', 1),
       ('NAVER D2', 'https://d2.naver.com/helloworld', null, 1),
       ('LY Corporation Tech Blog', 'https://techblog.lycorp.co.jp/ko',
        'https://techblog.lycorp.co.jp/ko/feed/index.xml', 1),
       ('Hyperconnect Tech Blog | 하이퍼커넥트의 기술블로그입니다.', 'https://hyperconnect.github.io',
        'https://hyperconnect.github.io/feed.xml', 1),
       ('직방 기술 블로그', 'https://medium.com/zigbang', 'https://medium.com/feed/zigbang', 1),
       ('뱅크샐러드 공식 블로그 | 기술 블로그', 'https://blog.banksalad.com/tech',
        'https://blog.banksalad.com/rss.xml', 1),
       ('SOCAR Tech Blog', 'https://tech.socarcorp.kr', 'https://tech.socarcorp.kr/feed.xml', 1),
       ('YOGIYO Tech Blog - 요기요 기술블로그', 'https://techblog.yogiyo.co.kr',
        'https://techblog.yogiyo.co.kr/feed', 1),
       ('토스 기술 블로그, 토스 테크', 'https://toss.tech', 'https://toss.tech/rss.xml', 1),
       ('여기어때 기술블로그', 'https://techblog.gccompany.co.kr', 'https://techblog.gccompany.co.kr/feed',
        1),
       ('당근 테크 블로그 – Medium', 'https://medium.com/daangn', 'https://medium.com/feed/daangn', 1),
       ('WATCHA – Medium', 'https://medium.com/watcha', 'https://medium.com/feed/watcha', 1),
       ('NHN Cloud Meetup', 'https://meetup.nhncloud.com', 'https://meetup.nhncloud.com/rss', 1),
       ('tech.kakao.com – 카카오테크, 미래의 문턱을 낮추는 기술', 'https://tech.kakao.com',
        'https://tech.kakao.com/feed/', 1),
       ('컬리 기술 블로그', 'http://thefarmersfront.github.io',
        'http://thefarmersfront.github.io/feed.xml', 1),
       ('쿠팡 엔지니어링 – Coupang Engineering Blog – Medium',
        'https://medium.com/coupang-engineering/kr/home', null, 1),
       ('기억보단 기록을', 'https://jojoldu.tistory.com', 'https://jojoldu.tistory.com/rss', 1);