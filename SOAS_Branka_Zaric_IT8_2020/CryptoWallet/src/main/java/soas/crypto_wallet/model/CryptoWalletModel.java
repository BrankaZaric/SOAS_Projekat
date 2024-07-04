package soas.crypto_wallet.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "crypto_wallet")
public class CryptoWalletModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;
	
	@OneToMany(mappedBy = "cryptoWallet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<CryptoValuesModel> values;
	
	@Column(unique = true, nullable = false)
	private String email;
	
	public CryptoWalletModel() {
		
	}
	
	public CryptoWalletModel(String email) {
		this.email = email;
	}

	public Long getWalletId() {
		return walletId;
	}

	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}

	public List<CryptoValuesModel> getValues() {
		return values;
	}

	public void setValues(List<CryptoValuesModel> values) {
		this.values = values;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
