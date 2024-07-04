
INSERT INTO bank_account (email) VALUES ('user@uns.ac.rs');
INSERT INTO bank_account (email) VALUES ('user2@uns.ac.rs');


INSERT INTO fiat_balance (bank_account_id, currency, balance) 
VALUES (1, 'EUR', 500.00), 
       (1, 'USD', 600.00), 
       (1, 'CHF', 0.00),
       (1, 'GBP', 0.00),
       (1, 'CAD', 0.00),
       (1, 'RSD', 100.00);


INSERT INTO fiat_balance (bank_account_id, currency, balance) 
VALUES (2, 'EUR', 0.00), 
       (2, 'USD', 0.00), 
       (2, 'GBP', 0.00),
       (2, 'CHF', 0.00),
       (2, 'CAD', 0.00),
       (2, 'RSD', 0.00);
