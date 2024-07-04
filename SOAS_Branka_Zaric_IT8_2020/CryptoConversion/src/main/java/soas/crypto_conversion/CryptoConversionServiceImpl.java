package soas.crypto_conversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import api.dtos.BankAccountDto;
import api.dtos.CryptoExchangeDto;
import api.dtos.CryptoWalletDto;
import api.dtos.CurrencyExchangeDto;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CryptoExchangeProxy;
import api.feignProxies.CryptoWalletProxy;
import api.feignProxies.CurrencyExchangeProxy;
import api.feignProxies.UsersProxy;
import api.services.CryptoConversionService;
import feign.FeignException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@RestController
public class CryptoConversionServiceImpl implements CryptoConversionService{

private RestTemplate template = new RestTemplate();
	
	@Autowired
	private CryptoExchangeProxy exchangeProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;

	@Autowired
	private UsersProxy usersProxy;

	
	CurrencyExchangeDto response;
	Retry retry;
	
	public CryptoConversionServiceImpl(RetryRegistry registry) {
		this.retry = registry.retry("default");
	}
	
	@Override
	public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
		// TODO Auto-generated method stub
		return null;
	}

	/*@Override
	public ResponseEntity<?> getConversionFeign(String from, String to, BigDecimal quantity,
			String authorizationHeader) {
		// TODO Auto-generated method stub
		

	    try {
	        String user = usersProxy.getCurrentUserRole(authorizationHeader);

	        if (user.equals("USER")) {

	            String userEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
	            BigDecimal accountCryptoAmount = walletProxy.getUserCryptoAmount(userEmail, from);

	            if (accountCryptoAmount.compareTo(quantity) >= 0) { // Compare using compareTo method
	                ResponseEntity<CryptoExchangeDto> response = exchangeProxy.getExchange(from, to);
	                CryptoExchangeDto responseBody = response.getBody();

	                BigDecimal exchangeValue = responseBody.getExchangeValue();
	                BigDecimal totalExchanged = exchangeValue.multiply(quantity); // Correct usage of multiply method

	                ResponseEntity<?> updatedValues = walletProxy.updateWalletCurrency(userEmail, from, to, quantity, totalExchanged);

	                return ResponseEntity.ok("Conversion was successful!"); // Return a simple success message
	            } else {
	                String errorMessage = "User doesn't have enough amount on his bank account for exchanging.";
	                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	            }
	        } else {
	            String errorMessage = "User is not allowed to perform exchanging since he is not 'USER'.";
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	        }
	    } catch (FeignException ex) {
	        return ResponseEntity.status(ex.status()).body(ex.getMessage());
	    }
	}*/
	
	@Override
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity, @RequestHeader("Authorization") String authorizationHeader) {
	    try {
	        String user = usersProxy.getCurrentUserRole(authorizationHeader);

	        if (!"USER".equals(user)) {
	            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is not allowed to perform exchanging since they are not 'USER'.");
	        }

	        String userEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
	        CryptoWalletDto wallet = walletProxy.getWalletByEmail(userEmail);

	        if (wallet == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found for user.");
	        }

	        BigDecimal walletCurrencyAmount = walletProxy.getUserCryptoAmount(userEmail, from);
	        if (walletCurrencyAmount.compareTo(quantity) < 0) {
	            return ResponseEntity.status(HttpStatus.CONFLICT).body("User doesn't have enough amount in the wallet for exchanging.");
	        }

	        ResponseEntity<CryptoExchangeDto> response = exchangeProxy.getExchange(from, to);
	        
	        if (response == null || response.getBody() == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exchange service response is null.");
	        }

	        CryptoExchangeDto responseBody = response.getBody();
	        BigDecimal exchangeValue = responseBody.getExchangeValue();
	        BigDecimal totalExchanged = exchangeValue.multiply(quantity);

	        ResponseEntity<?> updatedWallet = walletProxy.updateWalletCurrency(userEmail, from, to, quantity, totalExchanged);
	        
	        if (!updatedWallet.getStatusCode().is2xxSuccessful()) {
	            return ResponseEntity.status(updatedWallet.getStatusCode()).body("Failed to update wallet.");
	        }

	        //String successMessage = String.format("Successfully exchanged %s: %s for %s: %s", from, quantity, to, totalExchanged);
	        String message = "Conversion was successfull! " + quantity + from + " is exchanged for " + to;
	        
	        //return ResponseEntity.ok(successMessage);
	        
	        return ResponseEntity.ok().body(new Object() {
				public Object getBody() {
					return updatedWallet.getBody();
				}
				public String getMessage() {
					return message;
				}
			});

	    } catch (FeignException ex) {
	        // Log the exception stack trace
	        ex.printStackTrace();

	        // Return a generic error message with HTTP status from the exception
	        return ResponseEntity.status(ex.status()).body(ex.getMessage());
	    } catch (Exception ex) {
	        // Log any other unexpected exceptions
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
	    }
	}


}
