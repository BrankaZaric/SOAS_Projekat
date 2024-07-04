package currencyConversion;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyExchangeDto;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CurrencyExchangeProxy;
import api.feignProxies.UsersProxy;
import api.services.CurrencyConversionService;
import feign.FeignException;
import feign.Headers;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.NoDataFoundException;
import util.exceptions.ServiceUnavailableException;

import api.dtos.BankAccountDto;
import api.dtos.FiatBalanceDto;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {
	
	private RestTemplate template = new RestTemplate();
	
	
	@Autowired
	private CurrencyExchangeProxy exchangeProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;

	@Autowired
	private UsersProxy usersProxy;

	
	CurrencyExchangeDto response;
	Retry retry;
	
	public CurrencyConversionServiceImpl(RetryRegistry registry) {
		this.retry = registry.retry("default");
	}

	/*@Override
	public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
		// TODO Auto-generated method stub
		return null;
	}*/


	@Override
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity, @RequestHeader("Authorization") String authorizationHeader) {
	    try {
	        String user = usersProxy.getCurrentUserRole(authorizationHeader);

	        if (!"USER".equals(user)) {
	            //return ResponseEntity.status(HttpStatus.CONFLICT).body("User is not allowed to perform exchanging since they are not 'USER'.");
	        	throw new NoDataFoundException("User is not allowed to perform exchanging since they are not 'USER'.");
	        }

	        String userEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
	        BankAccountDto bankAccount = bankAccountProxy.getBankAccountByEmail(userEmail);

	        if (bankAccount == null) {
	           //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bank account not found for user.");
	        	throw new NoDataFoundException("Bank account not found for user.");
	        }

	        BigDecimal accountCurrencyAmount = bankAccountProxy.getUserCurrencyAmount(userEmail, from);
	        if (accountCurrencyAmount.compareTo(quantity) < 0) {
	            //return ResponseEntity.status(HttpStatus.CONFLICT).body("User doesn't have enough amount in the bank account for exchanging.");
	        	throw new NoDataFoundException("User doesn't have enough amount in the bank account for exchanging.");
	        }

	        ResponseEntity<CurrencyExchangeDto> response = exchangeProxy.getExchange(from, to);
	        
	        if (response == null || response.getBody() == null) {
	            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exchange service response is null.");
	        	 throw new ServiceUnavailableException("Exchange service response is null.");
	        }

	        CurrencyExchangeDto responseBody = response.getBody();
	        BigDecimal exchangeValue = responseBody.getExchangeValue();
	        BigDecimal totalExchanged = exchangeValue.multiply(quantity);

	        ResponseEntity<?> updatedBalances = bankAccountProxy.updateBalances(userEmail, from, to, quantity, totalExchanged);
	        
	        if (!updatedBalances.getStatusCode().is2xxSuccessful()) {
	            return ResponseEntity.status(updatedBalances.getStatusCode()).body("Failed to update balances.");
	        }

	        //String successMessage = String.format("Successfully exchanged %s: %s for %s: %s", from, quantity, to, totalExchanged);
	        String message = "Conversion was successfull! " + quantity + from + " is exchanged for " + to;
	        
	        //return ResponseEntity.ok(successMessage);
	        
	        return ResponseEntity.ok().body(new Object() {
				public Object getBody() {
					return updatedBalances.getBody();
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
