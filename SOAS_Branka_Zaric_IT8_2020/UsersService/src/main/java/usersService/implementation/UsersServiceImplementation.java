package usersService.implementation;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.CryptoWalletDto;
import api.dtos.UserDto;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CryptoWalletProxy;
import api.services.UsersService;
import usersService.model.UserModel;
import usersService.repository.UsersServiceRepository;
import util.exceptions.NoDataFoundException;
import util.exceptions.ServiceUnavailableException;

@RestController
public class UsersServiceImplementation implements UsersService {

	@Autowired
	private UsersServiceRepository repo;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;
	
	@Override
	public List<UserDto> getUsers() {
		List<UserModel> listOfModels = repo.findAll();
		ArrayList<UserDto> listOfDtos = new ArrayList<UserDto>();
		for(UserModel model: listOfModels) {
			listOfDtos.add(convertModelToDto(model));
		}
		return listOfDtos;
	}

	//KREIRANJE NOVOG KORISNIKA
	@Override
	public ResponseEntity<?> createUser(UserDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	    String role = extractRoleFromAuthorizationHeader(authorizationHeader);
	    
	    if ("USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
            //throw new ServiceUnavailableException("User does not have access to this service.");
        }
	    
	    UserModel user = convertDtoToModel(dto);

	    try {
	        switch (role) {
	            case "ADMIN":
	                // Only allow ADMIN to create users with role 'USER'
	                if (!"USER".equals(dto.getRole())) {
	                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin can only create users with role 'USER'.");
	                	//throw new ServiceUnavailableException("Admin can only create users with role 'USER'.");
	                }

	                // Check if user already exists
	                if (repo.existsById(user.getId())) {
	                    String errorMessage = "User with ID " + user.getId() + " already exists.";
	                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	                	//throw new NoDataFoundException("User with ID " + user.getId() + " already exists.");
	                }

	                // Save the user
	                UserModel createdUser = repo.save(user);

	                // Automatically create bank account
	                BankAccountDto bankAccountDto = new BankAccountDto();
	                bankAccountDto.setEmail(dto.getEmail());
	                ResponseEntity<?> bankAccountResponse = bankAccountProxy.createBankAccount(bankAccountDto, authorizationHeader);
	                if (!bankAccountResponse.getStatusCode().is2xxSuccessful()) {
	                    repo.delete(createdUser); // Rollback user creation if bank account creation fails
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create bank account for user.");
	                    //throw new ServiceUnavailableException("Failed to create bank account for user.");
	                }

	                // Automatically create crypto wallet
	                CryptoWalletDto cryptoWalletDto = new CryptoWalletDto();
	                cryptoWalletDto.setEmail(dto.getEmail());
	                ResponseEntity<?> cryptoWalletResponse = walletProxy.createWallet(cryptoWalletDto, authorizationHeader);
	                if (!cryptoWalletResponse.getStatusCode().is2xxSuccessful()) {
	                    repo.delete(createdUser); // Rollback user creation if crypto wallet creation fails
	                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create crypto wallet for user.");
	                    //throw new ServiceUnavailableException("Failed to create crypto wallet for user.");
	                }
	                
	                return ResponseEntity.ok(createdUser);

	            case "OWNER":
	                // Owners can only create, update, or delete users
	                // Owners are not authorized to create bank accounts
	                if (!"USER".equals(dto.getRole()) && !"ADMIN".equals(dto.getRole())) {
	                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Owner can only create users with role 'USER' or 'ADMIN'.");
	                	//throw new ServiceUnavailableException("Owner can only create users with role 'USER' or 'ADMIN'.");
	                }

	                // Save the user
	                createdUser = repo.save(user);
	                return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

	            default:
	                // For other roles (assuming default case for 'USER', etc.)
	                if ("OWNER".equals(user.getRole())) {
	                    if (repo.existsByRole("OWNER")) {
	                        return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
	                    	//throw new NoDataFoundException("A user with role 'OWNER' already exists.");
	                    }
	                }

	                // Check if user already exists
	                if (repo.existsById(user.getId())) {
	                    String errorMessage = "User with ID " + user.getId() + " already exists.";
	                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
	                	//throw new NoDataFoundException("User with ID " + user.getId() + " already exists.");
	                }

	                // Save the user
	                createdUser = repo.save(user);
	                return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
	    	//throw new ServiceUnavailableException("Error creating user: " + e.getMessage());
	    }
	}


	@Override
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UserDto dto, @RequestHeader("Authorization") String authorizationHeader) {
        String role = extractRoleFromAuthorizationHeader(authorizationHeader);
        
        if ("USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
        }
        
        UserModel user = repo.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found.");
        }

        if ("USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
        } else if ("ADMIN".equals(role)) {
            if ("USER".equals(user.getRole())) {
                if ("OWNER".equals(dto.getRole()) && repo.existsByRole("OWNER")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
                }
                user.setEmail(dto.getEmail());
                user.setPassword(dto.getPassword());
                user.setRole(dto.getRole());
                repo.save(user);
                
                return ResponseEntity.ok(convertModelToDto(user));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin can only update users with role 'USER'.");
            }
        } else if ("OWNER".equals(role)) {
            if ("OWNER".equals(dto.getRole()) && repo.existsByRole("OWNER") && !user.getRole().equals("OWNER")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
            } else {
                user.setEmail(dto.getEmail());
                user.setPassword(dto.getPassword());
                user.setRole(dto.getRole());
                repo.save(user);
                
                return ResponseEntity.ok(convertModelToDto(user));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role.");
        }
    }


	@Override
    public ResponseEntity<?> deleteUser(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        String role = extractRoleFromAuthorizationHeader(authorizationHeader);
        
        if ("USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
        }
        
        UserModel user = repo.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found.");
        }

        if ("USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
        } else if ("ADMIN".equals(role)) {
            if ("USER".equals(user.getRole())) {
                repo.deleteById(id);
                
                // Brisanje bankovnog računa korisnika
                bankAccountProxy.deleteAccount(user.getEmail()); // Poziv Feign klijenta za brisanje
                
                //Brisanje crypto wallet-a
                walletProxy.deleteWallet(user.getEmail());
                
                return ResponseEntity.ok("User with ID " + id + " has been deleted.");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin can only delete users with role 'USER'.");
            }
        } else if ("OWNER".equals(role)) {
            repo.deleteById(id);
            
         // Brisanje bankovnog računa korisnika
            bankAccountProxy.deleteAccount(user.getEmail()); // Poziv Feign klijenta za brisanje
            
            return ResponseEntity.ok("User with ID " + id + " has been deleted.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role.");
        }
    }

	
	
	public UserModel convertDtoToModel(UserDto dto) {
		return new UserModel(dto.getEmail(), dto.getPassword(), dto.getRole());
	}
	
	public UserDto convertModelToDto(UserModel model) {
		return new UserDto(model.getEmail(), model.getPassword(), model.getRole());
	}

	
	public String extractRoleFromAuthorizationHeader(String authorizationHeader) {
	    try {
	        String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
	        byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());
	        String decodedCredentials = new String(decodedBytes);
	        String[] credentials = decodedCredentials.split(":");
	        String email = credentials[0]; // prvo se unosi email kao username korisnika
	        UserModel user = repo.findByEmail(email);
	        if (user != null) {
	            return user.getRole();
	        } else {
	            System.out.println("User not found for email: " + email);
	            return null; // ili možete baciti izuzetak ili vratiti podrazumevanu ulogu
	        }
	    } catch (Exception e) {
	        System.out.println("Error extracting role: " + e.getMessage());
	        return null;
	    }
	}

	
	public String extractEmailFromAuthorizationHeader(String authorizationHeader) {
		String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.decode(encodedCredentials.getBytes());
		String decodedCredentials = new String(decodedBytes);
		String[] credentials = decodedCredentials.split(":");
		String role = credentials[0]; // prvo se unosi email kao username korisnika
		return role;
	}

	@Override
	 public Boolean getUser(String email) {
        UserModel user = repo.findByEmail(email);
        return user != null;
    }

	@Override
	public String getUsersRole(String email) {
        UserModel user = repo.findByEmail(email);
        if (user != null) {
            return user.getRole();
        } else {
            return "User not found";
        }
    }
	
	@Override
	public String getCurrentUserRole(String authorizationHeader) {
        String role = extractRoleFromAuthorizationHeader(authorizationHeader);
        if (role != null) {
            return role;
        } else {
            return "Unauthorized";
        }
    }
	

	@Override
	public String getCurrentUserEmail(String authorizationHeader) {
        String email = extractEmailFromAuthorizationHeader(authorizationHeader);
        if (email != null) {
            return email;
        } else {
            return "Unauthorized";
        }
    }
	
}
