package com.financialtransaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialtransaction.dto.CreateTransactionRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsProducerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public SqsProducerService(
            SqsClient sqsClient,
            ObjectMapper objectMapper) {

        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public String sendTransaction(CreateTransactionRequest request) {

        try {

            String messageBody =
                    objectMapper.writeValueAsString(request);

            SendMessageRequest sendRequest =
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)

                            // Transaction JSON
                            .messageBody(messageBody)

                            // Same account = same FIFO group
                            .messageGroupId(
                                    request.getAccountId().toString())

                            // Prevent duplicate transaction messages
                            .messageDeduplicationId(
                                    request.getTransactionId().toString())

                            .build();

            return sqsClient
                    .sendMessage(sendRequest)
                    .messageId();

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to serialize transaction", e);
        }
    }
}