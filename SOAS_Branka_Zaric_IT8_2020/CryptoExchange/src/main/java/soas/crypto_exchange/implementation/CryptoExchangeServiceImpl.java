package soas.crypto_exchange.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoExchangeDto;
import api.services.CryptoExchangeService;
import soas.crypto_exchange.model.CryptoExchangeModel;
import soas.crypto_exchange.repository.CryptoExchangeRepository;

@RestController
public class CryptoExchangeServiceImpl implements CryptoExchangeService {
	
	@Autowired
	private CryptoExchangeRepository repo;

	@Autowired
	private Environment environment;
	    
	@Override
	public ResponseEntity<CryptoExchangeDto> getExchange(@RequestParam String from,@RequestParam String to) {
		CryptoExchangeModel model = repo.findByFromAndTo(from, to);
		if (model == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(convertModelToDto(model));
	}
	
	public CryptoExchangeDto convertModelToDto(CryptoExchangeModel model) {
        CryptoExchangeDto dto = 
                new CryptoExchangeDto(model.getFrom(), model.getTo(), model.getExchangeValue());
        dto.setInstancePort(environment.getProperty("local.server.port"));
        return dto;
    }

}
