package com.financialtransaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialtransaction.dto.CreateTransactionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Service
public class SqsConsumerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public SqsConsumerService(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            TransactionService transactionService) {

        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedDelay = 10000)
    public void pollMessages() {

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(10)
                .visibilityTimeout(30)
                .build();

        List<Message> messages =
                sqsClient.receiveMessage(receiveRequest).messages();

        for (Message message : messages) {
            processMessage(message);
        }
    }

    private void processMessage(Message message) {

        try {

            System.out.println("======================================");
            System.out.println("Received SQS Message");
            System.out.println("Message ID: " + message.messageId());
            System.out.println("Body: " + message.body());
            System.out.println("======================================");

            CreateTransactionRequest request =
                    objectMapper.readValue(
                            message.body(),
                            CreateTransactionRequest.class
                    );

            // Process transaction inside PostgreSQL
            transactionService.processTransaction(request);

            // Delete ONLY after successful database processing
            deleteMessage(message);

            System.out.println(
                    "Transaction processed successfully: "
                            + request.getTransactionId()
            );

        } catch (Exception e) {

            System.err.println(
                    "Transaction processing failed: "
                            + e.getMessage()
            );

            /*
             * IMPORTANT:
             * Do NOT delete the message here.
             *
             * SQS will make the message visible again after
             * the visibility timeout.
             *
             * After the configured retry limit, SQS can move
             * the message to the DLQ.
             */
        }
    }

    private void deleteMessage(Message message) {

        DeleteMessageRequest deleteRequest =
                DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();

        sqsClient.deleteMessage(deleteRequest);
    }
}