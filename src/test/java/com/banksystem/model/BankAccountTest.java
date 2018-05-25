package com.banksystem.model;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalanceException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.AccountLockedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.fail;


public class BankAccountTest {

    private BankAccount bankAccount;

    @Before
    public void setup(){
        bankAccount = new BankAccount(888777666555L, 1);
    }


    @Test
    public void testCanGetUserId() {
        Assert.assertEquals(1, bankAccount.getUserId());
    }

    @Test
    public void testCanMakeBankAccount()  {
        Assert.assertEquals(888777666555L, bankAccount.getAccountNumber());
        MatcherAssert.assertThat(0.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test
    public void testDepositMoney() throws NegativeDepositException {
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test
    public void testThreadSafeDeposit() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, 1000).forEach(i -> callables.add(() -> {
                    try {
                        bankAccount.depositMoney(1.0);
                    } catch (NegativeDepositException e){
                        fail("The test threw an exception "+e.toString());
                    }
                    return 0;
                }
        ));
        executorService.invokeAll(callables);
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);

        MatcherAssert.assertThat(1000.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test(expected = NegativeDepositException.class)
    public void testDepositNegativeAmount() throws NegativeDepositException {
        bankAccount.depositMoney(-1.0);
    }

    @Test
    public void testWithdrawMoney() throws NegativeDepositException, WithdrawalExceedsBalanceException, AccountLockedException {
        bankAccount.depositMoney(100.0);
        bankAccount.withdrawMoney(50.0);
        MatcherAssert.assertThat(50.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test(expected = WithdrawalExceedsBalanceException.class)
    public void testWithdrawMoreThanBalance() throws NegativeDepositException, WithdrawalExceedsBalanceException, AccountLockedException {
        bankAccount.depositMoney(100.0);
        bankAccount.withdrawMoney(101.0);
    }

    @Test
    public void testWithdrawalIsThreadSafe() throws InterruptedException, NegativeDepositException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        bankAccount.depositMoney(1000.0);

        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, 1000).forEach(i -> callables.add(() -> {
                try {
                    bankAccount.withdrawMoney(1.0);
                } catch (WithdrawalExceedsBalanceException e){
                    fail("The test threw an exception "+e.toString());
                } catch (AccountLockedException e) {
                    fail("The test threw an exception "+e.toString());
                }
                return 0;
            }
        ));
        executorService.invokeAll(callables);
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test(expected = AccountLockedException.class)
    public void testWithdrawMoneyWhenAccountLocked() throws AccountLockedException, WithdrawalExceedsBalanceException {
        bankAccount.lock();
        bankAccount.withdrawMoney(10.0);
    }

    @Test
    public void testDepositMoneyWhenAccountLocked() throws NegativeDepositException{
        bankAccount.lock();
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(100.0));
    }

    @Test
    public void testCheckBalanceWhenAccountLocked() {
        bankAccount.lock();
        bankAccount.getAccountBalance();
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test
    public void testCheckBalanceAfterAccountUnlocked() {
        bankAccount.lock();
        bankAccount.unlock();
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test
    public void testWithdrawMoneyAfterAccountUnlocked() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalanceException {
        bankAccount.depositMoney(100.0);
        bankAccount.lock();
        bankAccount.unlock();
        bankAccount.withdrawMoney(10.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(90.0));
    }

    @Test
    public void testDepositMoneyAfterAccountUnlocked() throws NegativeDepositException {

        bankAccount.lock();
        bankAccount.unlock();
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(100.0));
    }
}
