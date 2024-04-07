DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;

DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_SEQ;

CREATE TABLE BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID BIGINT       NOT NULL PRIMARY KEY,
    VERSION         BIGINT COMMENT '낙관적 락에 사용되는 레코드 버전',
    JOB_NAME        VARCHAR(100) NOT NULL COMMENT '잡 이름',
    JOB_KEY         VARCHAR(32)  NOT NULL COMMENT '잡 이름 + 파라미터의 해시값. JobInstance를 고유하게 식별하는 데 사용하는 값',
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID BIGINT      NOT NULL PRIMARY KEY,
    VERSION          BIGINT COMMENT '낙관적 락에 사용되는 레코드 버전',
    JOB_INSTANCE_ID  BIGINT      NOT NULL COMMENT 'BATCH_JOB_INSTANCE.JOB_INSTANCE_ID',
    CREATE_TIME      DATETIME(6) NOT NULL COMMENT '생성 시각',
    START_TIME       DATETIME(6) DEFAULT NULL COMMENT '잡 실행의 시작 시각',
    END_TIME         DATETIME(6) DEFAULT NULL COMMENT '잡 실행의 완료 시각',
    STATUS           VARCHAR(10) COMMENT '잡 실행의 배치 상태',
    EXIT_CODE        VARCHAR(2500) COMMENT '잡 실행의 종료 코드',
    EXIT_MESSAGE     VARCHAR(2500) COMMENT 'EXIT_CODE와 관련된 메시지나 스택트레이스',
    LAST_UPDATED     DATETIME(6) COMMENT '수정 시각',
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID BIGINT       NOT NULL COMMENT 'BATCH_JOB_EXECUTION.JOB_EXECUTION_ID',
    PARAMETER_NAME   VARCHAR(100) NOT NULL COMMENT '파라미터 명',
    PARAMETER_TYPE   VARCHAR(100) NOT NULL COMMENT '파라미터 타입',
    PARAMETER_VALUE  VARCHAR(2500) COMMENT '파라미터 값',
    IDENTIFYING      CHAR(1)      NOT NULL COMMENT '잡의 고유성을 결정하는 데 사용되었는지 여부',
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  BIGINT       NOT NULL PRIMARY KEY,
    VERSION            BIGINT       NOT NULL COMMENT '낙관적 락에 사용되는 레코드 버전',
    STEP_NAME          VARCHAR(100) NOT NULL COMMENT '스텝 이름',
    JOB_EXECUTION_ID   BIGINT       NOT NULL COMMENT 'BATCH_JOB_EXECUTION.JOB_EXECUTION_ID',
    CREATE_TIME        DATETIME(6)  NOT NULL COMMENT '생성 시각',
    START_TIME         DATETIME(6) DEFAULT NULL COMMENT '시작 시각',
    END_TIME           DATETIME(6) DEFAULT NULL COMMENT '성공/실패 시각. null이면 오류가 발생했음을 의미함',
    STATUS             VARCHAR(10) COMMENT '상태',
    COMMIT_COUNT       BIGINT COMMENT '트랜잭션 커밋 횟수',
    READ_COUNT         BIGINT COMMENT '읽은 수',
    FILTER_COUNT       BIGINT COMMENT '필터링 된 item 수',
    WRITE_COUNT        BIGINT COMMENT '쓰고 커밋된 item 수',
    READ_SKIP_COUNT    BIGINT COMMENT '읽기를 건너 뛴 item 수',
    WRITE_SKIP_COUNT   BIGINT COMMENT '쓰기를 건너 뛴 item 수',
    PROCESS_SKIP_COUNT BIGINT COMMENT '처리를 건너 뛴 item 수',
    ROLLBACK_COUNT     BIGINT COMMENT '롤백 횟수. 이 횟수에는 재시도를 위한 롤백과 스킵 복구 절차에서의 롤백이 모두 포함됨',
    EXIT_CODE          VARCHAR(2500) COMMENT '종료 코드',
    EXIT_MESSAGE       VARCHAR(2500) COMMENT '잡 종료 메시지 / 스택트레이스',
    LAST_UPDATED       DATETIME(6) COMMENT '수정 시각',
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  BIGINT        NOT NULL PRIMARY KEY COMMENT 'BATCH_STEP_EXECUTION.STEP_EXECUTION_ID',
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL COMMENT 'SERIALIZED_CONTEXT 의 문자열 버전',
    SERIALIZED_CONTEXT TEXT COMMENT 'Step 실행과 관련된 컨텍스트 데이터를 직렬화된 형태로 저장',
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
) ENGINE = InnoDB comment 'Step의 ExecutionContext';

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL COMMENT 'SERIALIZED_CONTEXT 의 문자열 버전',
    SERIALIZED_CONTEXT TEXT COMMENT 'Job 실행과 관련된 컨텍스트 데이터를 직렬화된 형태로 저장',
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB comment 'Job의 ExecutionContext';

CREATE TABLE BATCH_STEP_EXECUTION_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_JOB_SEQ);
