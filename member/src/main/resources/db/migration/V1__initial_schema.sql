CREATE TABLE member
(
    id             BIGINT       NOT NULL,
    oauth_provider VARCHAR(255) NOT NULL,
    oauth_id       VARCHAR(255) NOT NULL,
    login_id       VARCHAR(255) NULL,
    password       VARCHAR(255) NULL,
    nickname       VARCHAR(255) NOT NULL,
    `role`         VARCHAR(255) NOT NULL,
    img_id         BIGINT NULL,
    is_deleted     BIT(1) NULL,
    img_url        VARCHAR(255) NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);

ALTER TABLE member
    ADD CONSTRAINT UK_MEMBER_NAME UNIQUE (nickname);
