package soas.bank_account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import soas.bank_account.model.BankAccountModel;

public interface BankAccountRepository extends JpaRepository<BankAccountModel, Long>{

	BankAccountModel findByEmail(String email);
	
}
