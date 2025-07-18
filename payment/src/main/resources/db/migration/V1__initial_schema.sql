CREATE TABLE payment
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    booking_id  BIGINT       NOT NULL,
    order_id    VARCHAR(64)  NOT NULL,
    payment_key VARCHAR(200) NOT NULL,
    amount      BIGINT       NOT NULL,
    order_name  VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL,
    approved_at datetime     NOT NULL,
    receipt_url VARCHAR(255) NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

ALTER TABLE payment
    ADD CONSTRAINT uc_payment_order UNIQUE (order_id);

ALTER TABLE payment
    ADD CONSTRAINT uc_payment_payment_key UNIQUE (payment_key);
