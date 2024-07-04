package api.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.BankAccountDto;
import feign.Headers;

public interface BankAccountService {

	@GetMapping("/bank-accounts")
	public ResponseEntity<List<BankAccountDto>> getAllAccounts();
	
	@GetMapping("/bank-accounts/{email}")
	BankAccountDto getBankAccountByEmail(@PathVariable("email") String email);
	
	//korisnik pregleda samo svoj racun
	@GetMapping("/bank-account/user")
	BankAccountDto getBankAccountForUser(@RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/bank-accounts")
    ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/bank-accounts/{email}")
    ResponseEntity<?> updateBankAccount(@PathVariable String email, @RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader);

    @DeleteMapping("/bank-accounts/{email}")
    //ResponseEntity<?> deleteBankAccount(@PathVariable String email, @RequestHeader("Authorization") String authorizationHeader);
    public void deleteAccount(@PathVariable("email") String email);
    
    /**/
    @GetMapping("/bank-account/{email}/{currencyFrom}")
	public BigDecimal getUserCurrencyAmount(@PathVariable("email") String email, @PathVariable("currencyFrom") String currencyFrom);

    /**/
    @PutMapping("/bank-account/account")
    public ResponseEntity<?> updateBalances(@RequestParam(value = "email") String email,
                                            @RequestParam(value = "from", required = false) String from,
                                            @RequestParam(value = "to", required = false) String to,
                                            @RequestParam(value = "quantity", required = false) BigDecimal quantity,
                                            @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount);
    
   
}
