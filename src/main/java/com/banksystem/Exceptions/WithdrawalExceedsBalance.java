package com.banksystem.Exceptions;

public class WithdrawalExceedsBalance extends Exception {
    public WithdrawalExceedsBalance(String s) {
        super(s);
    }
}
