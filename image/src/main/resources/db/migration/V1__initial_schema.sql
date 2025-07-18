CREATE TABLE image
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    url          VARCHAR(255) NULL,
    file_name    VARCHAR(255) NULL,
    content_type VARCHAR(255) NULL,
    size         BIGINT NULL,
    created_at   datetime NULL,
    is_deleted   BIT(1) NULL,
    CONSTRAINT pk_image PRIMARY KEY (id)
);
