package com.banksystem.repository;

import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class BankDataStoreTest {


    private BankDataStore dataStore;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        dataStore = new BankDataStore();
        Field field = BankDataStore.class.getDeclaredField("BANK_ACCOUNT_COUNTER");
        field.setAccessible(true);
        field.setLong(dataStore, 0);
    }

    @After
    public void tearDown() {
        dataStore = null;
    }

    @Test
    public void testGeneratedBankAccountNumber() throws Exception {

        Method generateBankAccountNumber = BankDataStore.class.getDeclaredMethod("generateBankAccountNumber");
        generateBankAccountNumber.setAccessible(true);
        long genValue = (long) generateBankAccountNumber.invoke(dataStore);
    }

    @Test
    public void testGeneratedBankAccountNumberIsThreadSafe() throws NoSuchMethodException, InterruptedException {


        Method generateBankAccountNumber = BankDataStore.class.getDeclaredMethod("generateBankAccountNumber");
        generateBankAccountNumber.setAccessible(true);
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(1);
        Set set = Collections.synchronizedSet(new HashSet());

        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                long genValue = 0;
                try {
                    latch.await();
                    genValue = (long) generateBankAccountNumber.invoke(dataStore);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                set.add(genValue);

            });
        }
        latch.countDown();
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);
        //If the generation of bank accounts was done in a synchronized manner the set would
        //contain an equal amount of bank account numbers to the number of calls the generate
        //bank account number method would have gotten.
        Assert.assertEquals(1000, set.size());

    }

    @Test
    public void testCanMakeNewAccount() {

        long accountNumber = dataStore.makeAccount(4);
        Assert.assertEquals(0L, accountNumber);
    }

    @Test
    public void testCanMakeMultipleNewAccount() {
        long accountNumber = dataStore.makeAccount(4);
        Assert.assertEquals(0L, accountNumber);
        accountNumber = dataStore.makeAccount(4);
        Assert.assertEquals(1L, accountNumber);
    }

    @Test
    public void testIsMakeAccountThreadSafeForSameUser() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                long genValue = 0;
                try {
                    latch.await();
                    dataStore.makeAccount(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
        }
        latch.countDown();
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);

        List<BankAccount> accounts = dataStore.getAllBankAccountsForUser(4);
        Assert.assertEquals(1000, accounts.size());
    }

    @Test
    public void testCanGetAccountCheckingCorrectAccountNumber() {

        long accountNumber = dataStore.makeAccount(4);
        BankAccount bankAccount = dataStore.getAccount(accountNumber);

        Assert.assertEquals(accountNumber, bankAccount.getAccountNumber());
    }

    @Test
    public void testCanGetAccountCheckingCorrectUserId() {

        long accountNumber = dataStore.makeAccount(4);
        BankAccount bankAccount = dataStore.getAccount(accountNumber);

        Assert.assertEquals(4, bankAccount.getUserId());
    }

    @Test
    public void testGetAccountThatDoesNotExist() {
        BankAccount bankAccount = dataStore.getAccount(1);
        Assert.assertNull(bankAccount);
    }

    @Test
    public void testGetAllBankAccounts() {
        User user = new User();
        user.setId(1);
        long accountNum1 = dataStore.makeAccount(user.getId());
        long accountNum2 = dataStore.makeAccount(user.getId());
        List<BankAccount> accounts = dataStore.getAllBankAccountsForUser(user.getId());
        Assert.assertEquals(accountNum1, accounts.get(0).getAccountNumber());
        Assert.assertEquals(accountNum2, accounts.get(1).getAccountNumber());
    }

    @Test
    public void testGetAllBankAccountsForInvalidId() {
        //No user has been created so there should be no accounts with this user ID
        List<BankAccount> accounts = dataStore.getAllBankAccountsForUser(1);
        Assert.assertNull(accounts);
    }

    @Test
    public void testCanStoreTransactionInfo() {
        dataStore.makeAccount(1);
        TransactionInfo tInfo = new TransactionInfo(0L, 1L, 100.0);
        Assert.assertTrue(dataStore.storeTransactionInfo(tInfo));
    }

    @Test
    public void testGetAllTransactionInfoOnAccountWithNoTransactions() {
        dataStore.makeAccount(1);
        List<TransactionInfo> transactions = dataStore.getAllTransactionInfo(0L);
        Assert.assertEquals(0, transactions.size());
    }

    @Test
    public void testGetAllTransactionInfoOnAccount() {
        dataStore.makeAccount(1);
        TransactionInfo tInfo = new TransactionInfo(0L, 1L, 100.0);
        dataStore.storeTransactionInfo(tInfo);
        List<TransactionInfo> transactions = dataStore.getAllTransactionInfo(0L);
        Assert.assertEquals(tInfo, transactions.get(0));
    }

    @Test
    public void testGetAllTransactionInfoOnAccountThatDoesNotExist() {
        List<TransactionInfo> transactions = dataStore.getAllTransactionInfo(0L);
        Assert.assertNull(transactions);
    }



}

