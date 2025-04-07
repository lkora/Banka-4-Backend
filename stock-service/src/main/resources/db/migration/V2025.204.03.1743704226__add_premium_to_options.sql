alter table options
    add column premium_amount numeric(38, 2) not null default 1.5;
alter table options
    add column premium_currency currency not null default 'USD';
