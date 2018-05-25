package com.banksystem.model;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class TransactionInfoTest {

    @Test
    public void testCanMakeTransactionInfoObject(){
        LocalDateTime datetime = LocalDateTime.now();
        TransactionInfo transactionInfo = new TransactionInfo("Insättning",100.0, datetime);
        Assert.assertEquals("Insättning", transactionInfo.getMessage());

        Assert.assertTrue(transactionInfo.getTransactionDate().isEqual(datetime));
        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(100.0));

    }

}
