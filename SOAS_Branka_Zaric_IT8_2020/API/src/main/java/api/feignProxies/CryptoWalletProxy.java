package api.feignProxies;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoWalletDto;

@FeignClient("crypto-wallet")
public interface CryptoWalletProxy {

	@GetMapping("/crypto-wallet/{email}")
	CryptoWalletDto getWalletByEmail(@PathVariable("email") String email);
	
	@PostMapping("/crypto-wallet")
	ResponseEntity<?> createWallet(@RequestBody CryptoWalletDto dto, @RequestHeader("Authorization") String authorizationHeader);
	
	@DeleteMapping("/crypto-wallet/{email}")
	public void deleteWallet(@PathVariable("email") String email);
	
	@GetMapping("/crypto-wallet/{email}/{cryptoFrom}")
	public BigDecimal getUserCryptoAmount(@PathVariable("email") String email, @PathVariable("cryptoFrom") String cryptoFrom);
	

    @PutMapping("/crypto-wallet/wallet")
    ResponseEntity<?> updateWalletCurrency(@RequestParam(value = "email") String email,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "quantity", required = false) BigDecimal quantity,
            @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount);
	
}
