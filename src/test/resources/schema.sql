create table "user" (
    id              uuid          not null,
    username        varchar(255)  not null,
    password        varchar(255)  not null,
    role            varchar(255)  not null,
    phone_number    varchar(255)          ,
    account_status  boolean,
    primary key (id)
);
