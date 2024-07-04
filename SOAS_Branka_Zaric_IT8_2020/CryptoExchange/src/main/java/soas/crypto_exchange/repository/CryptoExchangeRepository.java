package soas.crypto_exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import soas.crypto_exchange.model.CryptoExchangeModel;

public interface CryptoExchangeRepository extends JpaRepository<CryptoExchangeModel, Integer>{

	CryptoExchangeModel findByFromAndTo(String from, String to);
	
}
