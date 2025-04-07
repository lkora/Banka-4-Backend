alter table listings
    alter column exchange_id set not null;
alter table listings
    alter column security_id set not null;

alter table listing_daily_price_info
    alter column security_id set not null;

alter table orders
    add column used boolean not null default false;

alter table exchanges
    alter column created_at set not null;

alter table exchanges
    alter column created_at set default now();

alter table options
    alter column strike_price_amount set not null;

alter table options
    alter column strike_price_currency set not null;

alter table orders
    alter column asset_id set not null;

alter table stocks
    alter column created_at set not null;

alter table stocks
    alter column created_at set default now();
