create table `user`
(
    id                           bigint auto_increment primary key,
    email                        varchar(200) not null comment '이메일 주소',
    receive_days                 varchar(100) not null comment '수신 희망 요일',
    verification_code_expired_at datetime     not null default current_timestamp comment '이메일 인증코드 만료시각',
    created_at                   datetime     not null default current_timestamp comment '생성 시각',
    updated_at                   datetime     not null default current_timestamp comment '수정 시각',
    unique (email)
);

create table blog
(
    id               bigint auto_increment primary key,
    name             varchar(200) not null comment '이름',
    url              varchar(200) not null comment 'URL',
    rss              varchar(200) null comment 'RSS URL',
    url_css_selector varchar(200) null comment 'article.url을 크롤링하기 위한 css selector (RSS가 없을때 사용)',
    rss_status       varchar(20)  not null comment 'RSS 존재여부 / RSS 내 내용 존재 여부',
    created_by       bigint       not null comment '생성자 user.id',
    created_at       datetime     not null default current_timestamp comment '생성 시각',
    unique (url)
);

create table subscription
(
    id         bigint auto_increment primary key,
    user_id    bigint   not null comment 'user.id',
    blog_id    bigint   not null comment 'blog.id',
    created_at datetime not null default current_timestamp comment '구독 시각'
);

create table article
(
    id            bigint auto_increment primary key,
    blog_id       bigint        not null comment 'blog.id',
    title         varchar(200)  not null comment '제목',
    content       longtext      not null comment '내용',
    summary       varchar(1000) null comment '요약된 내용',
    summarized_by varchar(100)  null comment '요약된 내용 제공자',
    url           varchar(700)  not null comment 'URL',
    created_date  date          not null comment '생성 날짜',
    unique (url)
);

insert into `user`(email, password, verification_code, is_verified, receive_days)
values ('hwanghe159@gmail.com', null, '', true,
        'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY');
insert into blog (name, url, rss, url_css_selector, rss_status, created_by)
values ('우아한형제들 기술블로그', 'https://techblog.woowahan.com', 'https://techblog.woowahan.com/feed/',
        null, 'WITH_CONTENT', 1),
       ('NAVER D2', 'https://d2.naver.com/helloworld', null,
        '#container > div > div > div > div > h2 > a', 'NO_RSS', 1),
       ('LY Corporation Tech Blog', 'https://techblog.lycorp.co.jp/ko',
        'https://techblog.lycorp.co.jp/ko/feed/index.xml', null, 'WITHOUT_CONTENT', 1),
       ('Hyperconnect Tech Blog | 하이퍼커넥트의 기술블로그입니다.', 'https://hyperconnect.github.io',
        'https://hyperconnect.github.io/feed.xml', null, 'WITH_CONTENT', 1),
       ('뱅크샐러드 공식 블로그 | 기술 블로그', 'https://blog.banksalad.com/tech',
        'https://blog.banksalad.com/rss.xml', null, 'WITHOUT_CONTENT', 1),
       ('SOCAR Tech Blog', 'https://tech.socarcorp.kr', 'https://tech.socarcorp.kr/feed.xml', null,
        'WITH_CONTENT', 1),
       ('YOGIYO Tech Blog - 요기요 기술블로그', 'https://techblog.yogiyo.co.kr',
        'https://techblog.yogiyo.co.kr/feed', null, 'WITH_CONTENT', 1),
       ('토스 기술 블로그, 토스 테크', 'https://toss.tech', 'https://toss.tech/rss.xml', null, 'WITH_CONTENT',
        1),
       ('여기어때 기술블로그', 'https://techblog.gccompany.co.kr', 'https://techblog.gccompany.co.kr/feed',
        null, 'WITH_CONTENT', 1),
       ('당근 테크 블로그 – Medium', 'https://medium.com/daangn', 'https://medium.com/feed/daangn', null,
        'WITH_CONTENT', 1),
       ('WATCHA – Medium', 'https://medium.com/watcha', 'https://medium.com/feed/watcha', null,
        'WITH_CONTENT', 1),
       ('NHN Cloud Meetup', 'https://meetup.nhncloud.com', 'https://meetup.nhncloud.com/rss', null,
        'WITH_CONTENT', 1),
       ('tech.kakao.com – 카카오테크, 미래의 문턱을 낮추는 기술', 'https://tech.kakao.com',
        'https://tech.kakao.com/feed/', null, 'WITHOUT_CONTENT', 1),
       ('컬리 기술 블로그', 'http://thefarmersfront.github.io',
        'http://thefarmersfront.github.io/feed.xml', null, 'WITHOUT_CONTENT', 1),
       ('쿠팡 엔지니어링 – Coupang Engineering Blog – Medium',
        'https://medium.com/coupang-engineering/kr/home', null,
        'div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.u-lineHeightBase.postItem > a',
        'NO_RSS', 1),
       ('기억보단 기록을', 'https://jojoldu.tistory.com', 'https://jojoldu.tistory.com/rss', null,
        'WITH_CONTENT', 1),
       ('직방 기술 블로그', 'https://medium.com/zigbang', 'https://medium.com/feed/zigbang', null,
        'WITH_CONTENT', 1);

insert into subscription (user_id, blog_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 15),
       (1, 16),
       (1, 17);