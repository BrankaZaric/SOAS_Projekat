package soas.bank_account.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_account")
public class BankAccountModel {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(unique = true, nullable = false)
	    private String email;

	    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	    private List<FiatBalanceModel> fiatBalances;

	    public BankAccountModel() {
	    	
	    }

	    public BankAccountModel(String email) {
	        this.email = email;
	    }

	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

	    public List<FiatBalanceModel> getFiatBalances() {
	        return fiatBalances;
	    }

	    public void setFiatBalances(List<FiatBalanceModel> fiatBalances) {
	        this.fiatBalances = fiatBalances;
	    }

	    @Override
	    public String toString() {
	        return "BankAccount{" +
	                "id=" + id +
	                ", email='" + email + '\'' +
	                ", fiatBalances=" + fiatBalances +
	                '}';
	    }
    
}
