package api.dtos;

import java.math.BigDecimal;


public class CryptoValuesDto {

	private String crypto;
	
    private BigDecimal amount;
    
    
    public CryptoValuesDto() {
    	
    }

	public CryptoValuesDto(String crypto, BigDecimal amount) {
		super();
		this.crypto = crypto;
		this.amount = amount;
	}

	public String getCrypto() {
		return crypto;
	}

	public void setCrypto(String crypto) {
		this.crypto = crypto;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
    
    
}
