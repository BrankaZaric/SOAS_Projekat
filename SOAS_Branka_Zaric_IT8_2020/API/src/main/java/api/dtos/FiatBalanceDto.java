package api.dtos;

import java.math.BigDecimal;

public class FiatBalanceDto {

	private String currency;
    private BigDecimal balance;

    public FiatBalanceDto() {
    }

    public FiatBalanceDto(String currency, BigDecimal balance) {
        this.currency = currency;
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
