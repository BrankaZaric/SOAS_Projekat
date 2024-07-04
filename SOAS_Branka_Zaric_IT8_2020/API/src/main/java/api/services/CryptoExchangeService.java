package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoExchangeDto;

public interface CryptoExchangeService {

	 @GetMapping("/crypto-exchange")
	 ResponseEntity<CryptoExchangeDto> getExchange(@RequestParam String from, @RequestParam String to);
	 
}
