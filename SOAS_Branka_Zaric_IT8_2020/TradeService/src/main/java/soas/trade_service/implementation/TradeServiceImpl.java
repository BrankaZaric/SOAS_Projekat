package soas.trade_service.implementation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CryptoWalletProxy;
import api.feignProxies.UsersProxy;
import api.services.TradeService;
import feign.FeignException;
import soas.trade_service.model.TradeServiceModel;
import soas.trade_service.repository.TradeServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TradeServiceImpl implements TradeService {

	private static final Logger log = LoggerFactory.getLogger(TradeServiceImpl.class);
	
	@Autowired
	private TradeServiceRepository repository;

	@Autowired
	private UsersProxy userProxy;

	@Autowired
	private BankAccountProxy bankProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;
	
	@Override
	public ResponseEntity<?> tradeCurrenices(String from, String to, BigDecimal quantity, String authorizationHeader) {
		// TODO Auto-generated method stub
		 log.info("tradeCurrenices called with params: from={}, to={}, quantity={}, authorizationHeader={}", from, to, quantity, authorizationHeader);
		    
		try {
	        String user = userProxy.getCurrentUserRole(authorizationHeader);
	        
	        if(user != null) {
	            if (user.equals("USER")) {
	                String userEmail = userProxy.getCurrentUserEmail(authorizationHeader);
	                
	                if((from.equals("EUR") || from.equals("USD")) && (to.equals("BTC") || to.equals("ETH") || to.equals("LTC"))) {
	                    TradeServiceModel exchangeRate = getExchange(from, to);
	                    
	                    log.info("Exchange rate: {}", exchangeRate);
	                    
	                    if (exchangeRate == null) {
	                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exchange rate not found.");
	                    }
	                    
	                    /**/
	                    BigDecimal conversionMultiply = exchangeRate.getConversion();
	                    BigDecimal cryptoQuantity = conversionMultiply.multiply(quantity);
	                    
	                    log.info("Conversion multiply: {}, Crypto quantity: {}", conversionMultiply, cryptoQuantity);
	                    
	                 // Update bank account balances
	                    log.info("Calling updateBalances with params: userEmail={}, from={}, quantity={}", userEmail, from, quantity);
	                    ResponseEntity<?> updateAccountResponse;
	                    try {
	                        updateAccountResponse = bankProxy.updateBalances(userEmail, from, null, quantity, null);
	                        log.info("updateBalances response: {}", updateAccountResponse);
	                    } catch (FeignException e) {
	                        log.error("Failed to update bank account: {}", e.getMessage());
	                        StringWriter sw = new StringWriter();
	                        PrintWriter pw = new PrintWriter(sw);
	                        e.printStackTrace(pw);
	                        String stackTrace = sw.toString();
	                        log.error("FeignException stack trace: {}", stackTrace);
	                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
	                    }
                        
                        if (!updateAccountResponse.getStatusCode().is2xxSuccessful()) {
                            log.error("Failed to update bank account: {}", updateAccountResponse.getBody());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
                        }
	                    
	                    log.info("Update account response: {}", updateAccountResponse);
	                    
	                    // Update crypto wallet
	                    //BigDecimal cryptoQuantity = quantity.multiply(exchangeRate.getConversion());
	                    
	                    log.info("Calling updateWalletCurrency with params: userEmail={}, from={}, to={}, cryptoQuantity={}",
                                userEmail, null, to, cryptoQuantity);
	                    
	                    ResponseEntity<?> updateWalletResponse = walletProxy.updateWalletCurrency(userEmail, null, to, null, cryptoQuantity);
	                    
	                    if (!updateWalletResponse.getStatusCode().is2xxSuccessful()) {
	                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update crypto wallet.");
	                    }
	                    
	                    String mess = "Conversion is successful! " + quantity + " " + from + " exchanged for " + cryptoQuantity + " " + to;
	                    return ResponseEntity.ok().body(new Object() {
	                        public Object getBody() {
	                            return updateWalletResponse.getBody();
	                        }
	                        public String getMessage() {
	                            return mess;
	                        }
	                    });
	                } else if ((from.equals("BTC") || from.equals("ETH") || from.equals("LTC")) 
	                		&& (to.equals("EUR") || to.equals("USD"))) {
	                    TradeServiceModel exchangeRate = getExchange(from, to);
	                    if (exchangeRate == null) {
	                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Exchange rate not found.");
	                    }
	                    
	                    // Update crypto wallet
	                    ResponseEntity<?> updateWalletResponse = walletProxy.updateWalletCurrency(userEmail, from, null, quantity, null);
	                    if (!updateWalletResponse.getStatusCode().is2xxSuccessful()) {
	                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update crypto wallet.");
	                    }
	                    
	                    // Update bank account
	                    BigDecimal fiatQuantity = quantity.multiply(exchangeRate.getConversion());
	                    ResponseEntity<?> updateAccountResponse = bankProxy.updateBalances(userEmail, null, to, null, fiatQuantity);
	                    if (!updateAccountResponse.getStatusCode().is2xxSuccessful()) {
	                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update bank account.");
	                    }
	                    
	                    String mess = "Conversion is successful! " + quantity + " " + from + " exchanged for " + fiatQuantity + " " + to;
	                    return ResponseEntity.ok().body(new Object() {
	                        public Object getBody() {
	                            return updateAccountResponse.getBody();
	                        }
	                        public String getMessage() {
	                            return mess;
	                        }
	                    });
	                } else {
	                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid currency exchange request.");
	                }
	            } else {
	                String errorMessage = "User is not allowed to perform exchanging since they are not 'USER'.";
	                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	            }
	        } else {
	            String errorMessage = "User can't perform this action.";
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
	        }
	    } catch (FeignException e) {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        String stackTrace = sw.toString();

	        String errorMessage = "FeignException occurred: " + e.getMessage() + "\n" + stackTrace;
	        return ResponseEntity.status(e.status()).body(errorMessage);
	    }
	}

	public TradeServiceModel getExchange(String from, String to) {
		TradeServiceModel exchange = repository.findByFromAndToIgnoreCase(from, to);
		return exchange;
	}
}
