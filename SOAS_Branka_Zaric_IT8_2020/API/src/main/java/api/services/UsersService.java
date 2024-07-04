package api.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import api.dtos.UserDto;

public interface UsersService {

	@GetMapping("/users")
	List<UserDto> getUsers();
	
	@PostMapping("/users/newUser")
	ResponseEntity<?> createUser(@RequestBody UserDto dto,@RequestHeader String authorizationHeader);
	
	@PutMapping("/users/{id}")
	ResponseEntity<?> updateUser(@PathVariable int id,@RequestBody UserDto dto,@RequestHeader String authorizationHeader);
	
	@DeleteMapping("/users/{id}")
	ResponseEntity<?> deleteUser(@PathVariable int id,@RequestHeader("Authorization") String authorizationHeader);
	
	@GetMapping("/users/by-email/{email}")
	public Boolean getUser(@PathVariable("email") String email);
	
	@GetMapping("/users/by-email-role/{email}")
	public String getUsersRole(@PathVariable("email") String email);
	
	@GetMapping("/users/current-user-role")
	String getCurrentUserRole(@RequestHeader("Authorization") String authorizationHeader);
	
	@GetMapping("/users/current-user-email")
	String getCurrentUserEmail(@RequestHeader("Authorization") String authorizationHeader);
	
}
