create table Account (
  id VARCHAR(64),
  name VARCHAR(250),
  type VARCHAR(16),
  number VARCHAR(40),
  PRIMARY KEY (id)
);

create table AccTransaction (
  id VARCHAR(64),
  batch_id VARCHAR(64),
  account_id VARCHAR(64),
  transaction_ts TIMESTAMP,
  order_nr INTEGER,
  peer_id VARCHAR(64),
  category_id VARCHAR(64),
  amount INTEGER,
  balance INTEGER,
  comment VARCHAR(250),
  description VARCHAR(250),
  type VARCHAR(26),
  peer_accountNr VARCHAR(40),
  peer_name VARCHAR(64),
  terminal_name VARCHAR(64),
  terminal_location VARCHAR(64),
  terminal_card VARCHAR(64),
  PRIMARY KEY (id),
  CONSTRAINT trans_fk FOREIGN KEY (account_id) REFERENCES Account (id)
);

create table AccGroupMembers (
  groupId VARCHAR(64),
  memberId VARCHAR(64),
  PRIMARY KEY (groupId, memberId),
  CONSTRAINT member_fk FOREIGN KEY (memberId) REFERENCES Account (id),
    CONSTRAINT group_fk FOREIGN KEY (groupId) REFERENCES Account (id)
);

create table Peer (
  id   VARCHAR(64),
  name VARCHAR(120),
  PRIMARY KEY (id)
);

create table Category (
  id   VARCHAR(64),
  name VARCHAR(120),
  color VARCHAR(20),
  direction VARCHAR(10),
  PRIMARY KEY (id)
)
