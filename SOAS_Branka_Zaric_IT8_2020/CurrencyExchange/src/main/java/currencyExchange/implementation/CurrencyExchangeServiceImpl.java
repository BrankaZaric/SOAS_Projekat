package currencyExchange.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CurrencyExchangeDto;
import api.services.CurrencyExchangeService;
import currencyExchange.model.CurrencyExchangeModel;
import currencyExchange.repository.CurrencyExchangeRepository;

@RestController
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
	
	@Autowired
	private CurrencyExchangeRepository repo;
	
	@Autowired
	private Environment environment;

	/*@Override
	public ResponseEntity<?> getExchange(String from, String to) {
		CurrencyExchangeModel model = repo.findByFromAndTo(from, to);
		if(model == null) {
			return ResponseEntity.status(404).body(
					"Requested exchange pair [" + from + " into " + to + "] could not be found"
					);
		}
		return ResponseEntity.ok(convertModelToDto(model));
	}*/
	
	 @Override
	    public ResponseEntity<CurrencyExchangeDto> getExchange(String from, String to) {
	        CurrencyExchangeModel model = repo.findByFromAndTo(from, to);
	        if (model == null) {
	            return ResponseEntity.status(404).body(null);
	        }
	        return ResponseEntity.ok(convertModelToDto(model));
	    }
	
	public CurrencyExchangeDto convertModelToDto(CurrencyExchangeModel model) {
		CurrencyExchangeDto dto = 
				new CurrencyExchangeDto(model.getFrom(), model.getTo(), model.getExchangeValue());
		dto.setInstancePort(environment.getProperty("local.server.port"));
		return dto;
	}

}
