CREATE TABLE closed_stay_date
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    date       date NULL,
    listing_id BIGINT NULL,
    CONSTRAINT pk_closedstaydate PRIMARY KEY (id)
);

CREATE TABLE listing
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(255) NULL,
    price      INT NULL,
    start_date datetime NULL,
    end_date   datetime NULL,
    img_id     BIGINT NULL,
    host_id    BIGINT NULL,
    img_url    VARCHAR(255) NULL,
    type       SMALLINT NULL,
    max_guests INT NULL,
    status     SMALLINT NULL,
    address    VARCHAR(255) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    CONSTRAINT pk_listing PRIMARY KEY (id)
);

CREATE TABLE listing_comment
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    stay_id    BIGINT NULL,
    writer     VARCHAR(255) NULL,
    content    VARCHAR(255) NULL,
    created_at datetime NULL,
    rating DOUBLE NULL,
    status     SMALLINT NULL,
    CONSTRAINT pk_listing_comment PRIMARY KEY (id)
);

CREATE TABLE stay_tag
(
    stay_id BIGINT NOT NULL
);

CREATE TABLE tag
(
    id            BIGINT NULL,
    name          VARCHAR(255) NULL,
    `description` VARCHAR(255) NULL
);

ALTER TABLE closed_stay_date
    ADD CONSTRAINT FK_CLOSEDSTAYDATE_ON_LISTING FOREIGN KEY (listing_id) REFERENCES listing (id);

ALTER TABLE listing_comment
    ADD CONSTRAINT FK_LISTING_COMMENT_ON_STAY FOREIGN KEY (stay_id) REFERENCES listing (id);

ALTER TABLE stay_tag
    ADD CONSTRAINT fk_stay_tag_on_listing FOREIGN KEY (stay_id) REFERENCES listing (id);
