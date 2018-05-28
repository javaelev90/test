package com.banksystem.Exceptions;

public class NegativeDepositException extends IllegalArgumentException {
    public NegativeDepositException(String s) {
        super(s);
    }
}
