alter table wallets
    add column if not exists public_id uuid;

update wallets
set public_id = gen_random_uuid()
where public_id is null;

alter table wallets
    alter column public_id set not null;

alter table wallets
    add constraint uk_wallets_public_id unique (public_id);
