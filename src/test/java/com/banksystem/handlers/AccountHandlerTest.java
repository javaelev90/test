package com.banksystem.handlers;

import com.banksystem.Exceptions.NegativeDepositException;
import com.banksystem.Exceptions.WithdrawalExceedsBalance;
import com.banksystem.model.BankAccount;
import com.banksystem.model.TransactionInfo;
import com.banksystem.repository.BankDataStore;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.*;

import javax.security.auth.login.AccountLockedException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AccountHandlerTest {

    private AccountHandler accountHandler;
    private BankDataStore bankDataStore;

    @After
    public void tearDown(){
        accountHandler = null;
    }

    /**
     * Resets the static bank number counter which keeps incrementing when accounts are created
     * which also persists through tests
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Before
    public void resetStaticBankAccountNumber() throws NoSuchFieldException, IllegalAccessException {
        bankDataStore = new BankDataStore();
        Field field = BankDataStore.class.getDeclaredField("BANK_ACCOUNT_COUNTER");
        field.setAccessible(true);
        field.setLong(bankDataStore, 0);
    }

    /**
     * Sets up BankDataStore with numberOfAccounts for userId
     * @param numberOfAccounts
     * @param userId
     */
    private List<Long> setUpUserWithMultipleAccounts(int numberOfAccounts, int userId){

        List<Long> accountNumbers = new ArrayList<>();
        IntStream.range(0, numberOfAccounts).forEach(i ->accountNumbers.add(bankDataStore.makeAccount(userId)));
        accountHandler = new AccountHandler(bankDataStore);
        return accountNumbers;
    }

    /**
     * Sets up numberOfAccountsPerUser for numberOfUsers, the user ids will be 0 to < numberOfUsers
     * @param numberOfAccountsPerUser
     * @param numberOfUsers
     */
    private void setUpMultipleUsersWithMultipleAccounts(int numberOfAccountsPerUser, int numberOfUsers){
        IntStream.range(0, numberOfUsers).forEach(i ->setUpUserWithMultipleAccounts(numberOfAccountsPerUser, i));

    }

    @Test
    public void testGetAllBankAccount(){
        int userId = 5;
        setUpUserWithMultipleAccounts(2, userId);
        List<BankAccount> accounts = accountHandler.getAllAccountsForUser(userId);

        Assert.assertEquals(userId, accounts.get(0).getUserId());
        Assert.assertEquals(userId, accounts.get(1).getUserId());
        Assert.assertNotEquals(accounts.get(0).getAccountNumber(), accounts.get(1).getAccountNumber());
    }

    @Test
    public void testGetAccount(){
        int userId = 5;
        List<Long> accountNumbers = setUpUserWithMultipleAccounts(1, userId);
        BankAccount account = accountHandler.getAccount(accountNumbers.get(0));
        Assert.assertEquals(userId, account.getUserId());
    }

    @Test
    public void testDepositMoneyToAccount() throws NegativeDepositException, AccountLockedException {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);

        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(account.getAccountBalance()));
    }

    @Test
    public void testIfDepositTransactionWasSaved() throws NegativeDepositException, AccountLockedException {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);
        List<TransactionInfo> transactionInfoList = accountHandler.getTransactionsLog(account.getAccountNumber());
        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(transactionInfoList.get(0).getTransferAmount()));
    }

    @Test
    public void testWithdrawMoneyFromAccount() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);
        accountHandler.withdrawMoney(account.getAccountNumber(), 70.0);
        MatcherAssert.assertThat(30.0, CoreMatchers.equalTo(account.getAccountBalance()));
    }

    @Test
    public void testIfWithdrawalTransactionWasSaved() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);
        accountHandler.withdrawMoney(account.getAccountNumber(), 70.0);
        List<TransactionInfo> transactionInfoList = accountHandler.getTransactionsLog(account.getAccountNumber());
        // Check second index in transactionInfoList since a deposit was made before
        MatcherAssert.assertThat(-70.0, CoreMatchers.equalTo(transactionInfoList.get(1).getTransferAmount()));
    }

    private BankAccount helperMakeAccountAndAddMoneyToThenReturnIt(double amount) throws NegativeDepositException, AccountLockedException {
        int userId = 5;
        List<Long> accountNumbers = setUpUserWithMultipleAccounts(1, userId);
        //Beware that the depositMoney adds a transactionInfo object
        accountHandler.depositMoney(accountNumbers.get(0), amount);
        BankAccount account = accountHandler.getAccount(accountNumbers.get(0));
        return account;
    }

    @Test
    public void testTransferMoney() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        double amount = 30.0;
        BankAccount account1 = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);
        BankAccount account2 = helperMakeAccountAndAddMoneyToThenReturnIt(0.0);

        accountHandler.transferMoney(account1.getAccountNumber(), account2.getAccountNumber(), amount);
        MatcherAssert.assertThat((100.0-amount), CoreMatchers.equalTo(account1.getAccountBalance()));
        MatcherAssert.assertThat(amount, CoreMatchers.equalTo(account2.getAccountBalance()));
    }

    @Test
    public void testGetTransactionLogForTransaction() throws NegativeDepositException, AccountLockedException, WithdrawalExceedsBalance {
        double amount = 30.0;

        BankAccount fromAccount = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);
        BankAccount toAccount = helperMakeAccountAndAddMoneyToThenReturnIt(0.0);

        accountHandler.transferMoney(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), amount);

        List<TransactionInfo> transactionLog = accountHandler.getTransactionsLog(fromAccount.getAccountNumber());
        // Check index 1 since a deposit was made before in helperMakeAccountAndAddMoneyToThenReturnIt method
        Assert.assertEquals(transactionLog.get(1).getFromAccountNumber(), fromAccount.getAccountNumber());
        Assert.assertEquals(transactionLog.get(1).getToAccountNumber(), toAccount.getAccountNumber());
        //Should be a negative amount on fromAccounts log, the withdrawn amount
        MatcherAssert.assertThat(-amount, CoreMatchers.equalTo(transactionLog.get(1).getTransferAmount()));


        transactionLog = accountHandler.getTransactionsLog(toAccount.getAccountNumber());
        // Check index 1 since a deposit was made before in helperMakeAccountAndAddMoneyToThenReturnIt method
        Assert.assertEquals(transactionLog.get(1).getFromAccountNumber(), fromAccount.getAccountNumber());
        Assert.assertEquals(transactionLog.get(1).getToAccountNumber(), toAccount.getAccountNumber());
        //Should be a positive amount on toAccounts log, the deposited amount
        MatcherAssert.assertThat(amount, CoreMatchers.equalTo(transactionLog.get(1).getTransferAmount()));
    }





}
