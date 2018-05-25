package com.banksystem.Exceptions;

public class WithdrawalExceedsBalanceException extends Exception {
    public WithdrawalExceedsBalanceException(String s) {
        super(s);
    }
}
