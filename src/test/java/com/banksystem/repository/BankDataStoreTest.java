package com.banksystem.repository;

import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.model.User;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.fail;

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
        Assert.assertEquals(0L, genValue);
    }

    @Test
    public void testGeneratedBankAccountNumberIsThreadSafe() throws NoSuchMethodException, InterruptedException {


        Method generateBankAccountNumber = BankDataStore.class.getDeclaredMethod("generateBankAccountNumber");
        generateBankAccountNumber.setAccessible(true);

        Set<Long> set = Collections.synchronizedSet(new HashSet<>());

        ExecutorService executorService = Executors.newFixedThreadPool(5000);

        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, 5000).forEach(i -> callables.add(() -> {
                long genValue = 0;
                try {
                    genValue = (long) generateBankAccountNumber.invoke(dataStore);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    fail("The test threw an exception "+e.toString());
                }
            return set.add(genValue);
            }
        ));
        executorService.invokeAll(callables);

        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);
        //If the generation of bank accounts was done in a synchronized manner the set would
        //contain an equal amount of bank account numbers to the number of calls the generate
        //bank account number method would have gotten.
        Assert.assertEquals(5000, set.size());

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

        ExecutorService executorService = Executors.newFixedThreadPool(5000);

        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, 5000).forEach(i -> callables.add(() -> {
                dataStore.makeAccount(4);
                return 0;
            }
        ));
        executorService.invokeAll(callables);

        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);

        List<BankAccount> accounts = dataStore.getAllBankAccountsForUser(4);
        Assert.assertEquals(5000, accounts.size());
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
        Assert.assertEquals(2, accounts.size());
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
        TransactionInfo tInfo = new TransactionInfo("deposit", 100.0, LocalDateTime.now());
        Assert.assertTrue(dataStore.storeTransactionInfo(tInfo, 0L));
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
        LocalDateTime localDateTime = LocalDateTime.now();
        String message = "deposit";
        double amount = 100.0;
        TransactionInfo tInfo = new TransactionInfo(message, amount, localDateTime);
        dataStore.storeTransactionInfo(tInfo, 0L);
        List<TransactionInfo> transactions = dataStore.getAllTransactionInfo(0L);
        Assert.assertEquals(localDateTime, transactions.get(0).getTransactionDate());
        Assert.assertEquals(message, transactions.get(0).getMessage());
        MatcherAssert.assertThat(amount, CoreMatchers.equalTo(transactions.get(0).getTransferAmount()));

    }

    @Test
    public void testGetAllTransactionInfoOnAccountThatDoesNotExist() {
        List<TransactionInfo> transactions = dataStore.getAllTransactionInfo(0L);
        Assert.assertNull(transactions);
    }



}

