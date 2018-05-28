package com.banksystem.Exceptions;

public class WithdrawalExceedsBalanceException extends IllegalArgumentException {
    public WithdrawalExceedsBalanceException(String s) {
        super(s);
    }
}
