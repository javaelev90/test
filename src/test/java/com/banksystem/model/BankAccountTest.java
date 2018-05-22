package com.banksystem.model;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

public class BankAccountTest {

    @Test
    public void canMakeBankAccount(){
        BankAccount bankAccount = new BankAccount(888777666555L);
        Assert.assertEquals(888777666555L, bankAccount.getAccountNumber());
        MatcherAssert.assertThat(0D, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }


}
