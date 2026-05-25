create table if not exists user_entities (
    id varchar(1000) character set ascii not null,
    name varchar(100) not null,
    display_name varchar(200),
    primary key (id)
);

create table if not exists user_credentials (
    credential_id varchar(1000) character set ascii not null,
    user_entity_user_id varchar(1000) character set ascii not null,
    public_key blob not null,
    signature_count bigint,
    uv_initialized boolean,
    backup_eligible boolean not null,
    authenticator_transports varchar(1000),
    public_key_credential_type varchar(100),
    backup_state boolean not null,
    attestation_object blob,
    attestation_client_data_json blob,
    created timestamp null,
    last_used timestamp null,
    label varchar(1000) not null,
    primary key (credential_id)
);