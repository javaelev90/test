package com.banksystem.model;

/**
 * Used to store information about all transactions
 *
 * If fromAccountNumber and toAccountNumber is the same it means
 * it's the user who deposited or withdrew money for the same account
 *
 * The transfer amount will be negative for withdraws and positive for deposits
 */
public class TransactionInfo {

    private long fromAccountNumber;
    private long toAccountNumber;
    private double transferAmount;

    public TransactionInfo(long fromAccountNumber, long toAccountNumber, double transferAmount) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.transferAmount = transferAmount;
    }

    public long getFromAccountNumber() {
        return fromAccountNumber;
    }

    public long getToAccountNumber() {
        return toAccountNumber;
    }

    public double getTransferAmount() {
        return transferAmount;
    }
}
