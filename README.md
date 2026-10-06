# Amazon SQS FIFO-Based Financial Transaction Processing System

## 📌 Overview

This project demonstrates how Amazon Simple Queue Service (SQS) FIFO queues can be used to process financial transactions in a reliable and ordered manner.

The system processes deposit and withdrawal transactions sequentially and updates the account balance based on the processed transactions.

## 🎯 Objectives

- Maintain transaction ordering
- Process financial transactions reliably
- Prevent duplicate transaction processing
- Demonstrate AWS SQS FIFO queue functionality
- Integrate AWS services with Python using Boto3

## 🛠️ Technologies Used

- Python
- Amazon SQS FIFO
- AWS CLI
- Boto3
- Amazon Web Services

## 🏗️ Architecture

```text
User / Transaction
       |
       v
Python Application
       |
       v
Boto3
       |
       v
Amazon SQS FIFO Queue
       |
       v
Transaction Processor
       |
       v
Account Balance
