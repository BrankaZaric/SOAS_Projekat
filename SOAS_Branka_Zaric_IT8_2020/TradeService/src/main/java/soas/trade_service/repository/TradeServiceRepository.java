package soas.trade_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import soas.trade_service.model.TradeServiceModel;

public interface TradeServiceRepository extends JpaRepository<TradeServiceModel,Long>{

	TradeServiceModel findByFromAndToIgnoreCase(String from, String to);

}
