package util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParameters(MissingServletRequestParameterException ex){
		return ResponseEntity.status(400).body(
				new ExceptionModel(HttpStatus.BAD_REQUEST, ex.getMessage())
				);
	}
	
	@ExceptionHandler(NoDataFoundException.class)
	public ResponseEntity<?> handleNoDataFound(NoDataFoundException ex){
		return ResponseEntity.status(404).body(
				new ExceptionModel(HttpStatus.NOT_FOUND, ex.getMessage())
				);
	}
	
	@ExceptionHandler(ServiceUnavailableException.class)
	public ResponseEntity<?> handleUnavailableService(ServiceUnavailableException ex){
		return ResponseEntity.status(503).body(
				new ExceptionModel(HttpStatus.NOT_FOUND, ex.getMessage())
				);
	}
}
