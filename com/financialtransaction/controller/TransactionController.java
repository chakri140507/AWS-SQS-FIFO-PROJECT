package com.financialtransaction.controller;

import com.financialtransaction.dto.CreateTransactionRequest;
import com.financialtransaction.entity.Transaction;
import com.financialtransaction.service.SqsProducerService;
import com.financialtransaction.service.TransactionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SqsProducerService sqsProducerService;

    public TransactionController(
            TransactionService transactionService,
            SqsProducerService sqsProducerService) {

        this.transactionService = transactionService;
        this.sqsProducerService = sqsProducerService;
    }

    // Existing direct transaction endpoint
    @PostMapping
    public ResponseEntity<Transaction> processTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction =
                transactionService.processTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }

    // New SQS FIFO endpoint
    @PostMapping("/queue")
    public ResponseEntity<Map<String, Object>> queueTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        String messageId =
                sqsProducerService.sendTransaction(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "message", "Transaction sent to SQS",
                        "messageId", messageId,
                        "transactionId", request.getTransactionId(),
                        "accountId", request.getAccountId()
                ));
    }
}