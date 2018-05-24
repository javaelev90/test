package com.banksystem.model;

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
