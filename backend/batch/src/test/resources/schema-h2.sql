create table `user`
(
    id           bigint auto_increment primary key,
    email        varchar(200)                       not null,
    social_type  varchar(10)                        not null,
    social_id    varchar(100)                       not null,
    receive_days varchar(100)                       not null,
    created_at   datetime default CURRENT_TIMESTAMP not null,
    updated_at   datetime default CURRENT_TIMESTAMP not null,
    unique (email)
);

create table blog
(
    id               bigint auto_increment primary key,
    name             varchar(200)                       not null,
    url              varchar(200)                       not null,
    image            varchar(200),
    rss              varchar(200),
    url_css_selector varchar(200),
    rss_status       varchar(20)                        not null,
    is_show_on_main  boolean default false              not null,
    created_by       bigint                             not null,
    created_at       timestamp default CURRENT_TIMESTAMP not null,
    unique (url)
);

create table subscription
(
    id         bigint auto_increment primary key,
    user_id    bigint                             not null,
    blog_id    bigint                             not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    constraint udx_user_blog unique (user_id, blog_id)
);
create index idx_blog_id on subscription (blog_id);
create index idx_user_id on subscription (user_id);

create table article
(
    id            bigint auto_increment primary key,
    blog_id       bigint                             not null,
    title         varchar(1000)                      not null,
    content       clob                               not null,
    summary       varchar(2000),
    summarized_by varchar(100),
    url           varchar(700)                       not null,
    created_date  date                               not null,
    created_at    timestamp default CURRENT_TIMESTAMP not null,
    unique (url)
);
create index idx_created_date on article (created_date);

insert into `user`(email, social_type, social_id, receive_days)
values ('hwanghe159@gmail.com', 'GOOGLE', 1,
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