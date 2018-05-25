package com.banksystem.model;

import java.time.LocalDateTime;

/**
 * Used to store information about a transaction, the amount and the time
 *
 *
 * The transfer amount will be negative for withdraws and positive for deposits
 */
public class TransactionInfo {

    private double transferAmount;
    private String message;
    private LocalDateTime transactionDate;

    public TransactionInfo(String message, double transferAmount, LocalDateTime localDateTime) {
        this.message = message;
        this.transferAmount = transferAmount;
        transactionDate = localDateTime;
    }
    public double getTransferAmount() {
        return transferAmount;
    }

    public LocalDateTime getTransactionDate(){
        return transactionDate;
    }

    public String getMessage() {
        return message;
    }


    public String toString(){
        return "Amount: "+ transferAmount+" | "+"Time: "+transactionDate.toString()+" | "+"Message: "+message;
    }
}
