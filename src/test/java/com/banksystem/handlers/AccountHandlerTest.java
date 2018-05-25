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
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.fail;

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
     * @throws NoSuchFieldException reflection exception
     * @throws IllegalAccessException reflection exception
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
     * @param numberOfAccounts number of accounts
     * @param userId user id
     */
    private List<Long> setUpUserWithMultipleAccounts(int numberOfAccounts, int userId){

        List<Long> accountNumbers = new ArrayList<>();
        IntStream.range(0, numberOfAccounts).forEach(i ->accountNumbers.add(bankDataStore.makeAccount(userId)));
        accountHandler = new AccountHandler(bankDataStore);
        return accountNumbers;
    }

    /**
     * Sets up numberOfAccountsPerUser for numberOfUsers, the user ids will be 0 to < numberOfUsers
     * @param numberOfAccountsPerUser number of accounts per user
     * @param numberOfUsers number of users
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
    public void testDepositMoneyToAccount() throws NegativeDepositException {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(100.0);

        MatcherAssert.assertThat(100.0, CoreMatchers.equalTo(account.getAccountBalance()));
    }

    @Test
    public void testIfDepositTransactionWasSaved() throws NegativeDepositException {
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

    private BankAccount helperMakeAccountAndAddMoneyToThenReturnIt(double amount) throws NegativeDepositException {
        int userId = 5;
        List<Long> accountNumbers = setUpUserWithMultipleAccounts(1, userId);
        //Beware that the depositMoney adds a transactionInfo object
        accountHandler.depositMoney(accountNumbers.get(0), amount);
        return accountHandler.getAccount(accountNumbers.get(0));
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
        //Should be a negative amount on fromAccounts log, the withdrawn amount
        MatcherAssert.assertThat(-amount, CoreMatchers.equalTo(transactionLog.get(1).getTransferAmount()));


        transactionLog = accountHandler.getTransactionsLog(toAccount.getAccountNumber());
        // Check index 1 since a deposit was made before in helperMakeAccountAndAddMoneyToThenReturnIt method
        //Should be a positive amount on toAccounts log, the deposited amount
        MatcherAssert.assertThat(amount, CoreMatchers.equalTo(transactionLog.get(1).getTransferAmount()));
    }

    /**
     * Runs thread batches in default case it runs 100 thread batch 50 times. Resulting in 5000 threads run.
     *
     * You have to make sure that the fromAccount has the same amount of money as the number of threads being run.
     *
     * @param fromAccount where money should be transferred from
     * @param toAccount where money should be transferred to
     * @throws InterruptedException will fail test if this is thrown
     */
    private void helperRunTransferMoneyOnThreads(BankAccount fromAccount, BankAccount toAccount, int numberOfThreads) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, numberOfThreads).forEach(i -> callables.add(() -> {
                try {
                    accountHandler.transferMoney(fromAccount.getAccountNumber(),
                            toAccount.getAccountNumber(), 1.0);
                } catch (NegativeDepositException | WithdrawalExceedsBalance e1) {
                    fail("Threw exception"+e1.toString());
                }
            return 0;
            }
        ));
        executorService.invokeAll(callables);

    }

    @Test
    public void testIsTransferMoneyThreadSafe() throws NegativeDepositException, InterruptedException {

        BankAccount fromAccount = helperMakeAccountAndAddMoneyToThenReturnIt(5000.0);
        BankAccount toAccount = helperMakeAccountAndAddMoneyToThenReturnIt(0.0);

        helperRunTransferMoneyOnThreads(fromAccount, toAccount, 5000);

        MatcherAssert.assertThat(0.0, CoreMatchers.equalTo(fromAccount.getAccountBalance()));
        MatcherAssert.assertThat(5000.0, CoreMatchers.equalTo(toAccount.getAccountBalance()));

    }

    private void assertTransactionLogsAreSequentialForAccount(long accountNumber){
        List<TransactionInfo> transactionInfoList = accountHandler.getTransactionsLog(accountNumber);
        LocalDateTime localDateTime = transactionInfoList.get(0).getTransactionDate();
        for(TransactionInfo transactionInfo : transactionInfoList){
            Assert.assertTrue(localDateTime.isBefore(transactionInfo.getTransactionDate()) ||
                    localDateTime.isEqual(transactionInfo.getTransactionDate()));
            localDateTime = transactionInfo.getTransactionDate();
        }
    }

    @Test
    public void testIsTransferMoneyStoreTransactionSequential() throws NegativeDepositException, InterruptedException {

        BankAccount fromAccount = helperMakeAccountAndAddMoneyToThenReturnIt(5000.0);
        BankAccount toAccount = helperMakeAccountAndAddMoneyToThenReturnIt(0.0);

        helperRunTransferMoneyOnThreads(fromAccount, toAccount, 5000);


        //Check to account
        assertTransactionLogsAreSequentialForAccount(fromAccount.getAccountNumber());

        assertTransactionLogsAreSequentialForAccount(toAccount.getAccountNumber());

    }

    @Test
    public void testIsWithdrawStoreTransactionSequential() throws InterruptedException, NegativeDepositException {
        BankAccount account = helperMakeAccountAndAddMoneyToThenReturnIt(5000.0);
        ExecutorService executorService = Executors.newFixedThreadPool(5000);

        List<Callable<Object>> callables = new ArrayList<>();
        IntStream.range(0, 5000).forEach(i -> callables.add(() -> {
                try {
                    accountHandler.withdrawMoney(account.getAccountNumber(), 1.0);
                } catch (WithdrawalExceedsBalance | AccountLockedException e1 ) {
                    fail("Threw exception" + e1.toString());
                }
            return 0;
            }
        ));
        executorService.invokeAll(callables);

        assertTransactionLogsAreSequentialForAccount(account.getAccountNumber());
    }


}
