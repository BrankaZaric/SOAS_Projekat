package soas.bank_account.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiat_balance")
public class FiatBalanceModel {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "bank_account_id")
	    private BankAccountModel bankAccount;

	    @Column(nullable = true)
	    private String currency;

	    @Column(nullable = false)
	    private BigDecimal balance;

	    public FiatBalanceModel() {
	        
	    }

	    public FiatBalanceModel(String currency, BigDecimal balance) {
	        this.currency = currency;
	        this.balance = balance;
	    }

	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public BankAccountModel getBankAccount() {
	        return bankAccount;
	    }

	    public void setBankAccount(BankAccountModel bankAccount) {
	        this.bankAccount = bankAccount;
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

	    @Override
	    public String toString() {
	        return "FiatBalance{" +
	                "id=" + id +
	                ", bankAccount=" + bankAccount +
	                ", currency='" + currency + '\'' +
	                ", balance=" + balance +
	                '}';
	    }
	    
}
