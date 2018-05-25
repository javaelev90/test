package com.banksystem.model;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalance;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.AccountLockedException;
import java.util.concurrent.*;


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
    public void testCanMakeBankAccount() throws AccountLockedException {
        Assert.assertEquals(888777666555L, bankAccount.getAccountNumber());
        MatcherAssert.assertThat(0.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test
    public void testDepositMoney() throws NegativeDepositException, AccountLockedException {
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test
    public void testThreadSafeDeposit() throws InterruptedException, AccountLockedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(1);
        for(int i = 0; i < 1000; i++){
            executorService.submit(() -> {
                try {
                    latch.await();
                    bankAccount.depositMoney(10.0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NegativeDepositException e){
                    e.printStackTrace();
                }
            });
        }
        latch.countDown();
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);

        MatcherAssert.assertThat(10000.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test(expected = NegativeDepositException.class)
    public void testDepositNegativeAmount() throws NegativeDepositException, AccountLockedException {
        bankAccount.depositMoney(-1.0);
    }

    @Test
    public void testWithdrawMoney() throws NegativeDepositException, WithdrawalExceedsBalance, AccountLockedException {
        bankAccount.depositMoney(100.0);
        bankAccount.withdrawMoney(50.0);
        MatcherAssert.assertThat(50.0, CoreMatchers.equalTo(bankAccount.getAccountBalance()));
    }

    @Test(expected = WithdrawalExceedsBalance.class)
    public void testWithdrawMoreThanBalance() throws NegativeDepositException, WithdrawalExceedsBalance, AccountLockedException {
        bankAccount.depositMoney(100.0);
        bankAccount.withdrawMoney(101.0);
    }

    @Test
    public void testWithdrawalIsThreadSafe() throws InterruptedException, NegativeDepositException, AccountLockedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        bankAccount.depositMoney(1000.0);
        CountDownLatch latch = new CountDownLatch(1);
        for(int i = 0; i < 1000; i++){
            executorService.submit(() -> {
                try {
                    latch.await();
                    bankAccount.withdrawMoney(1.0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (WithdrawalExceedsBalance e){
                    e.printStackTrace();
                } catch (AccountLockedException e) {
                    e.printStackTrace();
                }
            });
        }
        latch.countDown();
        executorService.shutdown();
        assert executorService.awaitTermination(10, TimeUnit.SECONDS);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test(expected = AccountLockedException.class)
    public void testWithdrawMoneyWhenAccountLocked() throws AccountLockedException, WithdrawalExceedsBalance {
        bankAccount.lock();
        bankAccount.withdrawMoney(10.0);
    }

    @Test
    public void testDepositMoneyWhenAccountLocked() throws NegativeDepositException, AccountLockedException {
        bankAccount.lock();
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(100.0));
    }

    @Test
    public void testCheckBalanceWhenAccountLocked() throws AccountLockedException {
        bankAccount.lock();
        bankAccount.getAccountBalance();
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test
    public void testCheckBalanceAfterAccountUnlocked() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        bankAccount.lock();
        bankAccount.unlock();
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(0.0));
    }

    @Test
    public void testWithdrawMoneyAfterAccountUnlocked() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        bankAccount.depositMoney(100.0);
        bankAccount.lock();
        bankAccount.unlock();
        bankAccount.withdrawMoney(10.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(90.0));
    }

    @Test
    public void testDepositMoneyAfterAccountUnlocked() throws NegativeDepositException, AccountLockedException {

        bankAccount.lock();
        bankAccount.unlock();
        bankAccount.depositMoney(100.0);
        MatcherAssert.assertThat(bankAccount.getAccountBalance(), CoreMatchers.equalTo(100.0));
    }
}
