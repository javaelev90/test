package com.banksystem.repository;

import com.banksystem.model.BankAccount;
import com.banksystem.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class BankDataStoreTest {


    private BankDataStore dataStore;

    @Before
    public void setup(){
        dataStore = new BankDataStore();
    }

    @Test
    public void testGeneratedBankAccountNumber() throws Exception{
        BankDataStore bankDataStore = new BankDataStore();
        Method generateBankAccountNumber = BankDataStore.class.getDeclaredMethod("generateBankAccountNumber");
        generateBankAccountNumber.setAccessible(true);
        long genValue = (long)generateBankAccountNumber.invoke(bankDataStore);
    }

    @Test
    public void testGeneratedBankAccountNumberIsThreadSafe() throws NoSuchMethodException, InterruptedException{

        BankDataStore bankDataStore = new BankDataStore();
        Method generateBankAccountNumber = BankDataStore.class.getDeclaredMethod("generateBankAccountNumber");
        generateBankAccountNumber.setAccessible(true);
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(1);
        Set set = Collections.synchronizedSet(new HashSet());

        for(int i = 0; i < 1000; i++){
            executorService.submit(() -> {
                long genValue = 0;
                try {
                    latch.await();
                    genValue = (long)generateBankAccountNumber.invoke(bankDataStore);
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
    public void testCanMakeNewAccount(){
        User user = new User();
        user.setId(4);
        BankAccount bankAccount = dataStore.makeAccount(user);
        Assert.assertEquals(4, bankAccount.getUserId());
    }

    @Test
    public void testCanGetAccount(){
        User user = new User();
        user.setId(4);
        dataStore.makeAccount(user);
        

    }

//    @Test
//    public void testViewAllBankAccounts(){
//        User user = new User("Åsa", "Marklund");
//        dataStore.getAllBankAccounts();
//
//    }



}
