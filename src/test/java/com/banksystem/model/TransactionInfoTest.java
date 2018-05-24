package com.banksystem.model;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

public class TransactionInfoTest {

    @Test
    public void testCanMakeTransactionInfoObject(){
        TransactionInfo transactionInfo = new TransactionInfo(1L, 2L, 100.0);
        Assert.assertEquals(1L, transactionInfo.getFromAccountNumber());
        Assert.assertEquals(2L, transactionInfo.getToAccountNumber());
        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(100.0));
    }

}
