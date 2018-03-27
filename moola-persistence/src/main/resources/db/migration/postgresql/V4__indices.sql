create index AccTransaction__lookup on AccTransaction (
  account_id,
  transaction_ts,
  amount,
  comment,
  type
);