create table rule (
  id VARCHAR(64),
  expression VARCHAR(520),
  category_id varchar(64),
  peer_id varchar(64),
  PRIMARY KEY (id)
);

create table rulesbacklog (
  entry_id  VARCHAR(64),
  filter_id VARCHAR(64),
  PRIMARY KEY (entry_id, filter_id)
);