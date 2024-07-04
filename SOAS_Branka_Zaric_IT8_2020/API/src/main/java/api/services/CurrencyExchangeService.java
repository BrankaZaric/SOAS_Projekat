package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CurrencyExchangeDto;

public interface CurrencyExchangeService {

	@GetMapping("/currency-exchange")
	ResponseEntity<CurrencyExchangeDto> getExchange(@RequestParam String from, @RequestParam String to);
}
