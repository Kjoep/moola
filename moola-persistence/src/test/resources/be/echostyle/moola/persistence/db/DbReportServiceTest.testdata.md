#account

| id      | name    | type       | number |
|---------|---------|------------|--------|
| test    | Test    | CHECKING   |        |
| johnny  | JohnDoe | SAVINGS    |        |
| jeanie  | Jeanie  | INVESTMENT |        |
| groupie | Groupie | GROUPED    |        |

#accTransaction

| id   | batch_id | account_id | transaction_ts     | peer_id | category_id | amount | balance | comment               | description | type        | peer_accountnr | peer_name | terminal_name | terminal_location | terminal_card |
|------|----------|------------|--------------------|---------|-------------|--------|---------|-----------------------|-------------|-------------|----------------|-----------|---------------|-------------------|---------------|
| abc1 | abc123   | johnny     | 2016-2-12 20:00:00 | ringo   | groceries   | -2700  |  200    | I owe you some beers  |             | transfer    |                |           |               |                   |               |
| abc3 |          | johnny     | 2016-2-11 20:00:00 | target  | groceries   | -2100  |  200    | I owe you some nuts   |             | cardPayment |                |           |               |                   |               |
| abc5 |          | johnny     | 2016-2-12 19:00:00 | ringo   | salary      | 2700   |  200    | Pay                   |             | transfer    |                |           |               |                   |               |
| def2 |          | test       | 2016-3-12 20:00:00 | ringo   | holiday     | -620   |  200    | Vegas 17              |             | cardPayment |                |           |               |                   |               |
| def4 |          | test       | 2016-2-11 20:00:00 | ringo   | groceries   | -20    |  200    | Bought some condoms   |             | cardPayment |                |           |               |                   |               |
| def6 |          | test       | 2016-2-12 20:00:00 | ringo   | salary      | 1120   |  200    | Pay                   |             | transfer    |                |           |               |                   |               |


#accGroupMembers
| groupid  | memberid  |
|----------|-----------|
| groupie  | test      |
| groupie  | johnny    |
