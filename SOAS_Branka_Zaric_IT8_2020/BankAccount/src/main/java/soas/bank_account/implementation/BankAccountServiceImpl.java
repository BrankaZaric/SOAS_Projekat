package soas.bank_account.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.FiatBalanceDto;
import api.feignProxies.UsersProxy;
import api.services.BankAccountService;
import soas.bank_account.model.BankAccountModel;
import soas.bank_account.model.FiatBalanceModel;
import soas.bank_account.repository.BankAccountRepository;

@RestController
public class BankAccountServiceImpl implements BankAccountService{

	 private final BankAccountRepository repository;
	 private final UsersProxy usersProxy;

	    @Autowired
	    public BankAccountServiceImpl(BankAccountRepository repository, UsersProxy usersProxy) {
	        this.repository = repository;
	        this.usersProxy = usersProxy;
	    }

	    @Override
	    public ResponseEntity<List<BankAccountDto>> getAllAccounts() {
	        List<BankAccountModel> models = repository.findAll();
	        List<BankAccountDto> dtos = models.stream().map(this::convertToDto).collect(Collectors.toList());
	        return ResponseEntity.ok(dtos);
	    }

	    
	    @Override
	    public ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	        String role = usersProxy.getCurrentUserRole(authorizationHeader);

	        try {
	            if ("ADMIN".equals(role)) {
	                // Check if user with given email exists
	                Boolean userExists = usersProxy.getUser(dto.getEmail());
	                if (!userExists) {
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + dto.getEmail() + " does not exist.");
	                }

	                // Check if user already has a bank account
	                BankAccountModel existingAccount = repository.findByEmail(dto.getEmail());
	                if (existingAccount != null) {
	                    return ResponseEntity.badRequest().body("Bank account for user with email " + dto.getEmail() + " already exists.");
	                }

	                // Create new bank account with zero balances for all currencies
	                BankAccountModel bankAccount = new BankAccountModel(dto.getEmail());
	                List<FiatBalanceModel> initialBalances = createInitialBalances(bankAccount);
	                bankAccount.setFiatBalances(initialBalances);
	                repository.save(bankAccount);

	                return ResponseEntity.ok(Collections.singletonMap("message", "Bank account created successfully for user: " + dto.getEmail()));
	            }

	            // OWNER i drugi korisnici nemaju dozvolu za kreiranje bankovnih računa
	            if ("OWNER".equals(role) || !"USER".equals(role)) {
	                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Only users with role 'USER' can create bank accounts."));
	            }

	            Boolean userExists = usersProxy.getUser(dto.getEmail());
	            if (!userExists) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "User with email " + dto.getEmail() + " does not exist."));
	            }

	            BankAccountModel existingAccount = repository.findByEmail(dto.getEmail());
	            if (existingAccount != null) {
	                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Bank account for user with email " + dto.getEmail() + " already exists."));
	            }

	            BankAccountModel bankAccount = new BankAccountModel(dto.getEmail());
	            List<FiatBalanceModel> initialBalances = createInitialBalances(bankAccount);
	            bankAccount.setFiatBalances(initialBalances);
	            repository.save(bankAccount);

	            return ResponseEntity.ok(Collections.singletonMap("message", "Bank account created successfully for user: " + dto.getEmail()));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating bank account: " + e.getMessage()));
	        }
	    }

	  




		@Override
		public ResponseEntity<?> updateBankAccount(@PathVariable String email, @RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader) {
			 String role = usersProxy.getCurrentUserRole(authorizationHeader);
			    String currentUserEmail = usersProxy.getCurrentUserEmail(authorizationHeader);

			    if ("OWNER".equals(role)) {
			        return ResponseEntity.status(403).body("OWNER is not authorized to access this service.");
			    }

			    if ("USER".equals(role) && !currentUserEmail.equals(email)) {
			        return ResponseEntity.status(403).body("User can only update their own account.");
			    }

			    BankAccountModel bankAccount = repository.findByEmail(email);
			    if (bankAccount == null) {
			        return ResponseEntity.notFound().build();
			    }

			    // Clear the existing fiatBalances and add the new ones
			    bankAccount.getFiatBalances().clear();
			    for (FiatBalanceDto fiatBalanceDto : dto.getFiatBalances()) {
			        FiatBalanceModel fiatBalanceModel = new FiatBalanceModel(fiatBalanceDto.getCurrency(), fiatBalanceDto.getBalance());
			        fiatBalanceModel.setBankAccount(bankAccount); // Ensure the relationship is set
			        bankAccount.getFiatBalances().add(fiatBalanceModel);
			    }

			    repository.save(bankAccount);

			    return ResponseEntity.ok("Bank account updated successfully");
		}
		

		@Override
		public void deleteAccount(@PathVariable String email) {
			BankAccountModel bankAccount = repository.findByEmail(email);
			if (bankAccount != null) {
				repository.delete(bankAccount);
			}
		}
		
		/*public ResponseEntity<?> deleteBankAccount(@PathVariable String email, @RequestHeader("Authorization") String authorizationHeader) {
	        String role = usersProxy.getCurrentUserRole(authorizationHeader);
	        String currentUserEmail = usersProxy.getCurrentUserEmail(authorizationHeader);

	        if ("OWNER".equals(role)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OWNER is not authorized to access this service.");
	        }

	        if ("USER".equals(role) && !currentUserEmail.equals(email)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User can only delete their own account.");
	        }

	        BankAccountModel bankAccount = repository.findByEmail(email);
	        if (bankAccount == null) {
	            return ResponseEntity.notFound().build();
	        }

	        repository.delete(bankAccount);
	        return ResponseEntity.ok("Bank account deleted successfully");
	    }*/

		private BankAccountDto convertToDto(BankAccountModel model) {
		    BankAccountDto dto = new BankAccountDto();
		    dto.setEmail(model.getEmail());

		    if (model.getFiatBalances() != null) {
		        List<FiatBalanceDto> fiatBalanceDtos = model.getFiatBalances().stream()
		                .map(fiatBalanceModel -> {
		                    FiatBalanceDto fiatBalanceDto = new FiatBalanceDto();
		                    fiatBalanceDto.setCurrency(fiatBalanceModel.getCurrency());
		                    fiatBalanceDto.setBalance(fiatBalanceModel.getBalance());
		                    return fiatBalanceDto;
		                })
		                .collect(Collectors.toList());
		        dto.setFiatBalances(fiatBalanceDtos);
		    }

		    return dto;
		}


		private List<FiatBalanceModel> createInitialBalances(BankAccountModel bankAccount) {
		    List<FiatBalanceModel> balances = new ArrayList<>();
		    balances.add(new FiatBalanceModel("EUR", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("USD", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("GBP", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("CHF", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("CAD", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("RSD", BigDecimal.ZERO));
		    for (FiatBalanceModel balance : balances) {
		        balance.setBankAccount(bankAccount);
		    }
		    return balances;
		}

		 @Override
		    public BankAccountDto getBankAccountByEmail(String email) {
		        BankAccountModel bankAccount = repository.findByEmail(email);
		        if (bankAccount == null) {
		            return null; 
		        }
		        return convertToDto(bankAccount);
		    }

		 /**/
		@Override
		public BigDecimal getUserCurrencyAmount(String email, String currencyFrom) {
			BankAccountModel userAccount = repository.findByEmail(email);
			List<FiatBalanceModel> balances = userAccount.getFiatBalances();
			for (FiatBalanceModel accountCurrency : balances) {
				if (accountCurrency.getCurrency().equals(currencyFrom)) {
					return accountCurrency.getBalance();
				}
			}
			return null;
		}

		/*@Override
		public ResponseEntity<?> updateBalances(@RequestParam("email") String email,
                @RequestParam(value= "from", required = false) String from,
                @RequestParam(value= "to", required = false) String to,
                @RequestParam(value= "quantity", required = false) BigDecimal quantity,
                @RequestParam(value= "totalAmount", required = false) BigDecimal totalAmount){
			// TODO Auto-generated method stub
			
			BankAccountModel bankAccount = repository.findByEmail(email);

		    if (bankAccount == null) {
		        return ResponseEntity.notFound().build();
		    }

		    List<FiatBalanceModel> balances = bankAccount.getFiatBalances();

		    // Find the FiatBalanceModel for the 'from' currency
		    FiatBalanceModel fromBalance = balances.stream()
		            .filter(balance -> balance.getCurrency().equalsIgnoreCase(from))
		            .findFirst()
		            .orElse(null);

		    // Find the FiatBalanceModel for the 'to' currency
		    FiatBalanceModel toBalance = balances.stream()
		            .filter(balance -> balance.getCurrency().equalsIgnoreCase(to))
		            .findFirst()
		            .orElse(null);

		    if (fromBalance == null || toBalance == null) {
		        return ResponseEntity.badRequest().body("Invalid currency provided.");
		    }

		    // Perform balance update
		    BigDecimal newFromBalance = fromBalance.getBalance().subtract(quantity);
		    BigDecimal newToBalance = toBalance.getBalance().add(totalAmount);

		    fromBalance.setBalance(newFromBalance);
		    toBalance.setBalance(newToBalance);

		    // Save updated balances
		    repository.save(bankAccount);

		    return ResponseEntity.ok("Balances updated successfully.");
		}*/
		
		@Override
		public ResponseEntity<?> updateBalances(@RequestParam("email") String email,
		                                        @RequestParam(value = "from", required = false) String from,
		                                        @RequestParam(value = "to", required = false) String to,
		                                        @RequestParam(value = "quantity", required = false) BigDecimal quantity,
		                                        @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount) {

		    BankAccountModel bankAccount = repository.findByEmail(email);

		    if (bankAccount == null) {
		        return ResponseEntity.notFound().build();
		    }

		    List<FiatBalanceModel> balances = bankAccount.getFiatBalances();

		    if (from != null) {
		        // Smanji "from" valutu
		        FiatBalanceModel fromBalance = findFiatBalance(balances, from);
		        if (fromBalance == null) {
		            return ResponseEntity.badRequest().body("Currency '" + from + "' not found in user's account.");
		        }
		        BigDecimal newFromBalance = fromBalance.getBalance().subtract(quantity);
		        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
		            return ResponseEntity.badRequest().body("Insufficient funds in '" + from + "' balance.");
		        }
		        fromBalance.setBalance(newFromBalance);
		    }

		    if (to != null && totalAmount != null) {
		        // Povećaj "to" valutu
		        FiatBalanceModel toBalance = findFiatBalance(balances, to);
		        if (toBalance == null) {
		            return ResponseEntity.badRequest().body("Currency '" + to + "' not found in user's account.");
		        }
		        BigDecimal newToBalance = toBalance.getBalance().add(totalAmount);
		        toBalance.setBalance(newToBalance);
		    }

		    // Sačuvaj ažurirane balance
		    BankAccountModel updatedAccount = repository.save(bankAccount);
		    // Konvertuj ažurirani account u DTO
		    BankAccountDto updatedDto = convertToDto(updatedAccount);

		    return ResponseEntity.ok(updatedDto);
		}

		private FiatBalanceModel findFiatBalance(List<FiatBalanceModel> balances, String currency) {
		    return balances.stream()
		            .filter(balance -> balance.getCurrency().equalsIgnoreCase(currency))
		            .findFirst()
		            .orElse(null);
		}

		
		@Override
		public BankAccountDto getBankAccountForUser(String authorizationHeader) {
			// Proveravamo korisničku ulogu i email trenutnog korisnika iz authorizationHeader-a
		    String role = usersProxy.getCurrentUserRole(authorizationHeader);
		    String currentUserEmail = usersProxy.getCurrentUserEmail(authorizationHeader);

		    // Proveravamo da li korisnik ima ulogu USER i da li traži svoj sopstveni račun
		    if ("USER".equals(role)) {
		        return getBankAccountByEmail(currentUserEmail);
		    }

		    // Ako korisnik nije USER ili pokušava da pristupi tuđem računu, vraćamo null ili izuzetak
		    return null;
		}



	    
}
