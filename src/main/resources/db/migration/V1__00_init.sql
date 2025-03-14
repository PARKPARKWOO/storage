create table short_url
(
    id bigint primary key,
    short_url varchar(255) not null,
    resource_id varchar(100) not null
);

create table file_metadata
(
    file_id bigint primary key,
    content_length bigint not null,
    content_type varchar(100) not null ,
    uploaded_at timestamp not null ,
    file_name varchar(100) not null ,
    uploaded_by varchar(36) not null ,
    chunk_size int not null ,
    application_id bigint,
    page_size int
);

create table image_metadata
(
    file_id bigint primary key,
    content_length bigint not null,
    content_type varchar(100) not null ,
    uploaded_at timestamp not null ,
    file_name varchar(100) not null ,
    uploaded_by varchar(36) not null ,
    chunk_size int not null ,
    application_id bigint,
    page_size int
);

create table video_metadata
(
    file_id bigint primary key,
    content_length bigint not null,
    content_type varchar(100) not null ,
    uploaded_at timestamp not null ,
    file_name varchar(100) not null ,
    uploaded_by varchar(36) not null ,
    chunk_size int not null ,
    application_id bigint,
    page_size int
);