create table booking
(
    id                     bigint       not null auto_increment,
    listing_id             bigint       not null,
    guest_id               bigint       not null,
    order_id               varchar(64)  not null,
    checkin_at             date         not null,
    checkout_at            date         not null,
    stay_days              int          not null,
    guest_count            int          not null,
    payment_amount         bigint       not null,
    listing_title_snapshot varchar(200) not null,
    state                  varchar(20)  not null,
    created_at             timestamp    not null,
    updated_at             timestamp    not null,
    deleted                boolean      not null default false,

    primary key (id)
)
