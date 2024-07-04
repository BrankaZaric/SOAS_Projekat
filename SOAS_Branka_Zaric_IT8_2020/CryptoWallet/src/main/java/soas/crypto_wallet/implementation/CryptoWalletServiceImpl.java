package soas.crypto_wallet.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.CryptoValuesDto;
import api.dtos.CryptoWalletDto;
import api.dtos.FiatBalanceDto;
import api.feignProxies.UsersProxy;
import api.services.CryptoWalletService;
import soas.crypto_wallet.model.CryptoValuesModel;
import soas.crypto_wallet.model.CryptoWalletModel;
import soas.crypto_wallet.repository.CryptoWalletRepositoory;

@RestController
public class CryptoWalletServiceImpl implements CryptoWalletService{
	
	@Autowired
	private CryptoWalletRepositoory repository;
	
	@Autowired
	private UsersProxy userProxy;

	@Override
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets() {
		
		List<CryptoWalletModel> models = repository.findAll();
        List<CryptoWalletDto> dtos = models.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}
	
	@Override
	public CryptoWalletDto getWalletByEmail(String email) {
		// TODO Auto-generated method stub
		CryptoWalletModel wallet = repository.findByEmail(email);
        if (wallet == null) {
            return null; 
        }
        return convertToDto(wallet);
	}
	
	@Override
	public ResponseEntity<?> createWallet(CryptoWalletDto dto, String authorizationHeader) {
		String role = userProxy.getCurrentUserRole(authorizationHeader);

        try {
            if ("ADMIN".equals(role)) {
                // Check if user with given email exists
                Boolean userExists = userProxy.getUser(dto.getEmail());
                if (!userExists) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + dto.getEmail() + " does not exist.");
                }

                // Check if user already has a wallet
                CryptoWalletModel existingWallet = repository.findByEmail(dto.getEmail());
                if (existingWallet != null) {
                    return ResponseEntity.badRequest().body("Crypto wallet for user with email " + dto.getEmail() + " already exists.");
                }

                // Create new wallet with zero values for all currencies
                CryptoWalletModel wallet = new CryptoWalletModel(dto.getEmail());
                List<CryptoValuesModel> initialValues = createInitialValues(wallet);
                wallet.setValues(initialValues);
                repository.save(wallet);

                return ResponseEntity.ok(Collections.singletonMap("message", "Crypto wallet created successfully for user: " + dto.getEmail()));
            }

            // OWNER i drugi korisnici nemaju dozvolu za kreiranje 
            if ("OWNER".equals(role) || !"USER".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Only users with role 'USER' can create crypto wallet."));
            }

            Boolean userExists = userProxy.getUser(dto.getEmail());
            if (!userExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "User with email " + dto.getEmail() + " does not exist."));
            }

            CryptoWalletModel existingWallet = repository.findByEmail(dto.getEmail());
            if (existingWallet != null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Crypto wallet for user with email " + dto.getEmail() + " already exists."));
            }

            CryptoWalletModel wallet = new CryptoWalletModel(dto.getEmail());
            List<CryptoValuesModel> initialValues = createInitialValues(wallet);
            wallet.setValues(initialValues);
            repository.save(wallet);

            return ResponseEntity.ok(Collections.singletonMap("message", "Crypto wallet created successfully for user: " + dto.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating crypto wallet: " + e.getMessage()));
        }
	}

	/*@Override
	public ResponseEntity<?> updateWallet(long walletId, CryptoWalletDto updateWallet) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	@Override
	public ResponseEntity<?> updateWallet(String email, CryptoWalletDto dto, String authorizationHeader) {
		// TODO Auto-generated method stub
		String role = userProxy.getCurrentUserRole(authorizationHeader);
	    String currentUserEmail = userProxy.getCurrentUserEmail(authorizationHeader);

	    if ("OWNER".equals(role)) {
	        return ResponseEntity.status(403).body("OWNER is not authorized to access this service.");
	    }

	    if ("USER".equals(role) && !currentUserEmail.equals(email)) {
	        return ResponseEntity.status(403).body("User can only update their own account.");
	    }

	    CryptoWalletModel wallet = repository.findByEmail(email);
	    if (wallet == null) {
	        return ResponseEntity.notFound().build();
	    }

	    // Convert CryptoValuesDto to CryptoValuesModel
	    /*List<CryptoValuesModel> values = dto.getValues().stream()
	            .map(valuesDto -> new CryptoValuesModel(valuesDto.getCrypto(), valuesDto.getAmount()))
	            .collect(Collectors.toList());*/
	    
	    wallet.getValues().clear();
	    for (CryptoValuesDto cryptoValuesDto: dto.getValues()) {
	    	CryptoValuesModel cryptoValuesModel = new CryptoValuesModel(cryptoValuesDto.getCrypto(), cryptoValuesDto.getAmount());
	    	cryptoValuesModel.setCryptoWallet(wallet);
	    	wallet.getValues().add(cryptoValuesModel);
	    }

	    repository.save(wallet);

	    return ResponseEntity.ok("Crypto wallett updated successfully");
	}
	
	

	@Override
	public void deleteWallet(String email) {
		CryptoWalletModel wallet = repository.findByEmail(email);
		if (wallet != null) {
			repository.delete(wallet);
		}
	}

	@Override
	public BigDecimal getUserCryptoAmount(String email, String cryptoFrom) {
		CryptoWalletModel wallet = repository.findByEmail(email);
		List<CryptoValuesModel> values = wallet.getValues();
		for (CryptoValuesModel accountCrypto : values) {
			if (accountCrypto.getCrypto().equals(cryptoFrom)) {
				return accountCrypto.getAmount();
			}
		}
		return null;
	}

	/*@Override
	public ResponseEntity<?> updateWalletCurrency(@RequestParam(value = "email") String email,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "quantity", required = false) BigDecimal quantity,
            @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount) {
		// TODO Auto-generated method stub
		
		CryptoWalletModel wallet = repository.findByEmail(email);

	    if (wallet == null) {
	        return ResponseEntity.notFound().build();
	    }

	    List<CryptoValuesModel> values = wallet.getValues();

	    // Find the FiatBalanceModel for the 'from' currency
	    CryptoValuesModel fromValue = values.stream()
	            .filter(value -> value.getCrypto().equalsIgnoreCase(from))
	            .findFirst()
	            .orElse(null);

	    // Find the FiatBalanceModel for the 'to' currency
	    CryptoValuesModel toValue = values.stream()
	            .filter(value -> value.getCrypto().equalsIgnoreCase(to))
	            .findFirst()
	            .orElse(null);

	    if (fromValue == null || toValue == null) {
	        return ResponseEntity.badRequest().body("Invalid currency provided.");
	    }

	    // Perform balance update
	    BigDecimal newFromValue = fromValue.getAmount().subtract(quantity);
	    BigDecimal newToValue = toValue.getAmount().add(totalAmount);

	    fromValue.setAmount(newFromValue);
	    toValue.setAmount(newToValue);

	    // Save updated balances
	    repository.save(wallet);

	    return ResponseEntity.ok("Crypto values updated successfully.");
	    
	}*/
	
	/*@Override
	public ResponseEntity<?> updateWalletCurrency(@RequestParam(value = "email") String email,
	        @RequestParam(value = "from", required = false) String from,
	        @RequestParam(value = "to", required = false) String to,
	        @RequestParam(value = "quantity", required = false) BigDecimal quantity,
	        @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount) {

	    // Pronađi korisnički novčanik na osnovu email adrese
	    CryptoWalletModel wallet = repository.findByEmail(email);

	    if (wallet == null) {
	        return ResponseEntity.notFound().build();
	    }

	    List<CryptoValuesModel> values = wallet.getValues();

	    // Pronađi CryptoValuesModel za valutu 'from'
	    CryptoValuesModel fromValue = values.stream()
	            .filter(value -> value.getCrypto().equalsIgnoreCase(from))
	            .findFirst()
	            .orElse(null);

	    // Pronađi CryptoValuesModel za valutu 'to'
	    CryptoValuesModel toValue = values.stream()
	            .filter(value -> value.getCrypto().equalsIgnoreCase(to))
	            .findFirst()
	            .orElse(null);

	    if (fromValue == null || toValue == null) {
	        return ResponseEntity.badRequest().body("Invalid currency provided.");
	    }

	    // Izvrši matematičke operacije za ažuriranje količina valuta
	    BigDecimal newFromValue = fromValue.getAmount().subtract(quantity);
	    BigDecimal newToValue = toValue.getAmount().add(totalAmount);

	    fromValue.setAmount(newFromValue);
	    toValue.setAmount(newToValue);

	    // Sačuvaj ažurirane vrednosti u bazi podataka
	    repository.save(wallet);

	    return ResponseEntity.ok("Crypto values updated successfully.");
	}*/


	@Override
	public ResponseEntity<?> updateWalletCurrency(@RequestParam(value = "email") String email,
	                                              @RequestParam(value = "from", required = false) String from,
	                                              @RequestParam(value = "to", required = false) String to,
	                                              @RequestParam(value = "quantity", required = false) BigDecimal quantity,
	                                              @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount) {

	    // Pronađi korisnički novčanik na osnovu email adrese
	    CryptoWalletModel wallet = repository.findByEmail(email);

	    if (wallet == null) {
	        return ResponseEntity.notFound().build();
	    }

	    List<CryptoValuesModel> values = wallet.getValues();

	    if (from != null) {
	        // Smanji "from" valutu ako je ona kripto valuta
	        CryptoValuesModel fromValue = findCryptoValues(values, from);
	        if (fromValue == null) {
	            return ResponseEntity.badRequest().body("Crypto '" + from + "' not found in crypto wallet.");
	        }
	        BigDecimal newFromValue = fromValue.getAmount().subtract(quantity);
	        if (newFromValue.compareTo(BigDecimal.ZERO) < 0) {
	            return ResponseEntity.badRequest().body("Insufficient funds in '" + from + "' value.");
	        }
	        fromValue.setAmount(newFromValue);
	    }

	    if (to != null && totalAmount != null) {
	        // Povećaj "to" valutu ako je ona kripto valuta
	        CryptoValuesModel toValue = findCryptoValues(values, to);
	        if (toValue == null) {
	            return ResponseEntity.badRequest().body("Crypto '" + to + "' not found in crypto wallet.");
	        }
	        BigDecimal newToValue = toValue.getAmount().add(totalAmount);
	        toValue.setAmount(newToValue);
	    }

	    CryptoWalletModel updatedWallet = repository.save(wallet);
	    CryptoWalletDto updatedDto = convertToDto(updatedWallet);

	    return ResponseEntity.ok(updatedDto);
	}

	private CryptoValuesModel findCryptoValues(List<CryptoValuesModel> values, String crypto) {
	    return values.stream()
	            .filter(value -> value.getCrypto().equalsIgnoreCase(crypto))
	            .findFirst()
	            .orElse(null);
	}
	
	private CryptoWalletDto convertToDto(CryptoWalletModel model) {
		CryptoWalletDto dto = new CryptoWalletDto();
	    dto.setEmail(model.getEmail());

	    if (model.getValues() != null) {
	        List<CryptoValuesDto> cryptoValuesDtos = model.getValues().stream()
	                .map(cryptoValuesModel -> {
	                	CryptoValuesDto cryptoValuesDto = new CryptoValuesDto();
	                	cryptoValuesDto.setCrypto(cryptoValuesModel.getCrypto());
	                	cryptoValuesDto.setAmount(cryptoValuesModel.getAmount());
	                    return cryptoValuesDto;
	                })
	                .collect(Collectors.toList());
	        dto.setValues(cryptoValuesDtos);
	    }

	    return dto;
	}
	
	private List<CryptoValuesModel> createInitialValues(CryptoWalletModel wallet) {
	    List<CryptoValuesModel> values = new ArrayList<>();
	    values.add(new CryptoValuesModel("BTC", BigDecimal.ZERO));
	    values.add(new CryptoValuesModel("ETH", BigDecimal.ZERO));
	    values.add(new CryptoValuesModel("LTC", BigDecimal.ZERO));
	    for (CryptoValuesModel value : values) {
	    	value.setCryptoWallet(wallet);
	    }
	    return values;
	}

	
	//korisnik pregleda samo svoj wallet
	@Override
	public CryptoWalletDto getWalletForUser(String authorizationHeader) {
		// Proveravamo korisničku ulogu i email trenutnog korisnika iz authorizationHeader-a
	    String role = userProxy.getCurrentUserRole(authorizationHeader);
	    String currentUserEmail = userProxy.getCurrentUserEmail(authorizationHeader);
	    
	 // Proveravamo da li korisnik ima ulogu USER i da li traži svoj sopstveni račun
	    if ("USER".equals(role)) {
	        return getWalletByEmail(currentUserEmail);
	    }

	    // Ako korisnik nije USER ili pokušava da pristupi tuđem računu, vraćamo null ili izuzetak
	    return null;
	}

	
	
}
