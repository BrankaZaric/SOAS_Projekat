package api.dtos;

import java.util.List;


public class CryptoWalletDto {

	private List<CryptoValuesDto> values;
	
	private String email;
	
	public CryptoWalletDto() {
		
	}
	
	public CryptoWalletDto(List<CryptoValuesDto> values, String email) {
		super();
		this.values = values;
		this.email = email;
	}

	public List<CryptoValuesDto> getValues() {
		return values;
	}

	public void setValues(List<CryptoValuesDto> values) {
		this.values = values;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
