package com.financialtransaction.service;

import com.financialtransaction.dto.CreateTransactionRequest;
import com.financialtransaction.entity.Account;
import com.financialtransaction.entity.Transaction;
import com.financialtransaction.repository.AccountRepository;
import com.financialtransaction.repository.TransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction processTransaction(
            CreateTransactionRequest request) {

        // 1. Check for duplicate transaction
    	if (transactionRepository.existsById(
    	        request.getTransactionId())) {

    	    throw new ResponseStatusException(
    	            HttpStatus.CONFLICT,
    	            "Transaction already processed: "
    	                    + request.getTransactionId()
    	    );
    	}

        // 2. Find and lock the account row
        Account account = accountRepository
                .findByIdForUpdate(request.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Account not found: "
                                        + request.getAccountId()
                        ));

        // 3. Check account status
        if (!"ACTIVE".equals(account.getStatus())) {

            throw new RuntimeException(
                    "Account is not active"
            );
        }

        // 4. Get transaction details
        BigDecimal amount = request.getAmount();

        BigDecimal currentBalance = account.getBalance();

        String transactionType =
                request.getTransactionType().toUpperCase();

        // 5. Process transaction
        if ("DEPOSIT".equals(transactionType)) {

            account.setBalance(
                    currentBalance.add(amount)
            );

        } else if ("WITHDRAW".equals(transactionType)) {

            // Check sufficient balance
            if (currentBalance.compareTo(amount) < 0) {

                throw new RuntimeException(
                        "Insufficient balance"
                );
            }

            account.setBalance(
                    currentBalance.subtract(amount)
            );

        } else {

            throw new RuntimeException(
                    "Unsupported transaction type: "
                            + transactionType
            );
        }

        // 6. Save updated account
        accountRepository.save(account);

        // 7. Create transaction record
        Transaction transaction = new Transaction();

        transaction.setTransactionId(
                request.getTransactionId()
        );

        transaction.setAccountId(
                request.getAccountId()
        );

        transaction.setTransactionType(
                transactionType
        );

        transaction.setAmount(
                amount
        );

        transaction.setStatus(
                "SUCCESS"
        );

        transaction.setProcessedAt(
                LocalDateTime.now()
        );

        // 8. Save transaction record
        return transactionRepository.save(transaction);
    }
}