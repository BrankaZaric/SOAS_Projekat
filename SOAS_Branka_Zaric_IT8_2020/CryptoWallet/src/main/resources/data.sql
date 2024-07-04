
INSERT INTO crypto_wallet (email) VALUES ('user@uns.ac.rs');
INSERT INTO crypto_wallet (email) VALUES ('user2@uns.ac.rs');


INSERT INTO crypto_values (wallet_id, crypto, amount) 
VALUES (1, 'BTC', 500.00), 
       (1, 'ETH', 1000.00), 
       (1, 'LTC', 400.00);


INSERT INTO crypto_values (wallet_id, crypto, amount) 
VALUES (2, 'BTC', 0.00), 
       (2, 'ETH', 0.00), 
       (2, 'LTC', 0.00);
