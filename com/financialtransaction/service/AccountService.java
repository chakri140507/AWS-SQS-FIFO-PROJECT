package com.financialtransaction.service;

import com.financialtransaction.dto.CreateAccountRequest;
import com.financialtransaction.entity.Account;
import com.financialtransaction.repository.AccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {

        Account account = new Account();

        account.setCustomerName(request.getCustomerName());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    public Account getAccount(UUID accountId) {

        return accountRepository.findById(accountId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Account not found: " + accountId
                        )
                );
    }
}