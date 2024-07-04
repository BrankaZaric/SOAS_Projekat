package soas.crypto_wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import soas.crypto_wallet.model.CryptoWalletModel;

public interface CryptoWalletRepositoory extends JpaRepository<CryptoWalletModel, Long> {

	boolean existsByEmail(String email);

	CryptoWalletModel findByEmail(String email);
	
}
