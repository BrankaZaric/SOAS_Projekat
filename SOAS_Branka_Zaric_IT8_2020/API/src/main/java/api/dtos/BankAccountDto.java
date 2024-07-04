package api.dtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class BankAccountDto {

    private String email;

    private List<FiatBalanceDto> fiatBalances;

    public BankAccountDto() {
    	
    }

	public BankAccountDto(String email, List<FiatBalanceDto> fiatBalances) {
		super();
		this.email = email;
		this.fiatBalances = fiatBalances;
	}



	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<FiatBalanceDto> getFiatBalances() {
		return fiatBalances;
	}

	public void setFiatBalances(List<FiatBalanceDto> fiatBalances) {
		this.fiatBalances = fiatBalances;
	}
    
    
    
}
