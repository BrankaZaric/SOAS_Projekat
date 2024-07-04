package soas.crypto_wallet.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "crypto_values")
public class CryptoValuesModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long valueId;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_id")
    private CryptoWalletModel cryptoWallet;
	
	private String crypto;
	
    private BigDecimal amount;
    
    
    
    public CryptoValuesModel() {
    	
    }
    

	public CryptoValuesModel(String crypto, BigDecimal amount) {
		super();
		this.crypto = crypto;
		this.amount = amount;
	}



	public Long getValueId() {
		return valueId;
	}

	public void setValueId(Long valueId) {
		this.valueId = valueId;
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

	public CryptoWalletModel getCryptoWallet() {
		return cryptoWallet;
	}

	public void setCryptoWallet(CryptoWalletModel cryptoWallet) {
		this.cryptoWallet = cryptoWallet;
	}
	
    
}
