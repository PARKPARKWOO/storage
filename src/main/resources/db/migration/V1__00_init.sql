create table short_url
(
    id bigint not null  primary key,
    short_url varchar(255) not null,
    resource_id varchar(100) not null,
);