package com.klaytn.caver.scenario;

import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.fee.FeePayerManager;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.KlayRawTransaction;
import com.klaytn.caver.tx.model.TransactionTransformer;

import java.util.List;
import java.util.Random;

import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;

public class RoleBasedIT extends Scenario {
    protected final String MEMO = "Klaytn MemoTest 1234567890!";

    protected void roleBasedTransactionTest(TransactionGetter transactionGetter, ReceiptChecker receiptChecker, boolean isUpdateTest) throws Exception {
        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(1, 1, 1);
        List<KlayCredentials> senderCredentialsList = roleBaseAccountGenerator.getSenderCredentialForTest(isUpdateTest);

        TransactionManager transactionManager = new TransactionManager.Builder(caver, senderCredentialsList.get(0))
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        TransactionTransformer transactionTransformer = transactionGetter.get(roleBaseAccountGenerator.getAddress());
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(transactionTransformer);
        receiptChecker.check(transactionTransformer, transactionReceipt);
    }

    protected void feeDelegatedRoleBasedMultiTransactionSignerTest(TransactionGetter transactionGetter, ReceiptChecker receiptChecker, boolean isUpdateTest) throws Exception {
        AccountSizeGenerator accountSizeGenerator = new AccountSizeGenerator();
        int transactionAccountSize = accountSizeGenerator.getTransactionAccountSize();
        int updateAccountSize = accountSizeGenerator.getUpdateAccountSize();

        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(transactionAccountSize, updateAccountSize, 1);
        List<KlayCredentials> senderCredentialsList = roleBaseAccountGenerator.getSenderCredentialForTest(isUpdateTest);
        List<KlayCredentials> feePayerCredentialsList = roleBaseAccountGenerator.getFeePayerAccountCredential();

        KlayRawTransaction klayRawTransaction = null;
        KlayTransactionReceipt.TransactionReceipt transactionReceipt;
        TransactionTransformer transactionTransformer = null;

        for (int i = 0; i < senderCredentialsList.size(); i++) {
            TransactionManager transactionManager = new TransactionManager.Builder(caver, senderCredentialsList.get(i))
                    .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                    .setChaindId(LOCAL_CHAIN_ID)
                    .build();

            if (klayRawTransaction == null) {
                // 1. The transaction constructor creates and signs a transaction.
                transactionTransformer = transactionGetter.get(roleBaseAccountGenerator.getAddress());
                klayRawTransaction = transactionManager.sign(transactionTransformer);
            } else {
                // 2. Those with the RoleTransaction key receive and sign the rawTransaction.
                klayRawTransaction = transactionManager.sign(klayRawTransaction.getValueAsString());
            }
        }

        // 3. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerCredentialsList.get(0))
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        transactionReceipt = feePayerManager.executeTransaction(klayRawTransaction.getValueAsString());
        receiptChecker.check(transactionTransformer, transactionReceipt);
    }

    protected void feeDelegatedRoleBasedTransactionMultiFeePayerTest(TransactionGetter transactionGetter, ReceiptChecker receiptChecker, boolean isUpdateTest) throws Exception {
        AccountSizeGenerator accountSizeGenerator = new AccountSizeGenerator();
        int feePayerAccountSize = accountSizeGenerator.getFeePayerAccountSize();

        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(1, 1, feePayerAccountSize);
        List<KlayCredentials> senderCredentialsList = roleBaseAccountGenerator.getSenderCredentialForTest(isUpdateTest);
        List<KlayCredentials> feePayerCredentialsList = roleBaseAccountGenerator.getFeePayerAccountCredential();

        KlayRawTransaction klayRawTransaction = null;
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = null;

        // 1. The transaction constructor creates and signs a transaction.
        TransactionManager transactionManager = new TransactionManager.Builder(caver, senderCredentialsList.get(0))
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        TransactionTransformer transactionTransformer = transactionGetter.get(roleBaseAccountGenerator.getAddress());
        klayRawTransaction = transactionManager.sign(transactionTransformer);


        for (int i = 0; i < feePayerAccountSize; i++) {
            if (i < feePayerAccountSize - 1) {
                // 2. Those with the RoleFeePayer key receive and sign the rawTransaction.
                FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerCredentialsList.get(i))
                        .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                        .setChainId(LOCAL_CHAIN_ID)
                        .build();
                klayRawTransaction = feePayerManager.sign(klayRawTransaction.getValueAsString());
            } else {
                // 3. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
                FeePayerManager feePayerManagerForExcuter = new FeePayerManager.Builder(caver, feePayerCredentialsList.get(i))
                        .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                        .setChainId(LOCAL_CHAIN_ID)
                        .build();
                transactionReceipt = feePayerManagerForExcuter.executeTransaction(klayRawTransaction.getValueAsString());
            }
        }

        receiptChecker.check(transactionTransformer, transactionReceipt);
    }

    protected void feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(TransactionGetter transactionGetter, ReceiptChecker receiptChecker, boolean isUpdateTest) throws Exception {
        AccountSizeGenerator accountSizeGenerator = new AccountSizeGenerator();
        int transactionAccountSize = accountSizeGenerator.getTransactionAccountSize();
        int updateAccountSize = accountSizeGenerator.getUpdateAccountSize();
        int feePayerAccountSize = accountSizeGenerator.getFeePayerAccountSize();

        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(transactionAccountSize, updateAccountSize, feePayerAccountSize);
        List<KlayCredentials> senderCredentialsList = roleBaseAccountGenerator.getSenderCredentialForTest(isUpdateTest);
        List<KlayCredentials> feePayerCredentialsList = roleBaseAccountGenerator.getFeePayerAccountCredential();

        KlayRawTransaction klayRawTransaction = null;
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = null;
        TransactionTransformer transactionTransformer = null;

        for (int i = 0; i < senderCredentialsList.size(); i++) {
            TransactionManager transactionManager = new TransactionManager.Builder(caver, senderCredentialsList.get(i))
                    .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                    .setChaindId(LOCAL_CHAIN_ID)
                    .build();

            if (klayRawTransaction == null) {
                // 1. The transaction constructor creates and signs a transaction.
                transactionTransformer = transactionGetter.get(roleBaseAccountGenerator.getAddress());
                klayRawTransaction = transactionManager.sign(transactionTransformer);
            } else {
                // 2. Those with the RoleTransaction key receive and sign the rawTransaction.
                klayRawTransaction = transactionManager.sign(klayRawTransaction.getValueAsString());
            }
        }

        for (int i = 0; i < feePayerAccountSize; i++) {
            FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerCredentialsList.get(i))
                    .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                    .setChainId(LOCAL_CHAIN_ID)
                    .build();

            if (i < feePayerAccountSize - 1) {
                // 3. Those with the RoleFeePayer key receive and sign the rawTransaction.
                klayRawTransaction = feePayerManager.sign(klayRawTransaction.getValueAsString());
            } else {
                // 4. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
                transactionReceipt = feePayerManager.executeTransaction(klayRawTransaction.getValueAsString());
            }
        }

        receiptChecker.check(transactionTransformer, transactionReceipt);
    }

    protected void feeDelegatedRoleBasedTransactionTest(TransactionGetter transactionGetter, ReceiptChecker receiptChecker, boolean isUpdateTest) throws Exception {
        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(1, 1, 1);
        List<KlayCredentials> senderCredentialsList = roleBaseAccountGenerator.getSenderCredentialForTest(isUpdateTest);
        List<KlayCredentials> feePayerCredentialsList = roleBaseAccountGenerator.getFeePayerAccountCredential();

        // 1. The transaction constructor creates and signs a transaction.
        TransactionManager transactionManager = new TransactionManager.Builder(caver, senderCredentialsList.get(0))
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        TransactionTransformer transactionTransformer = transactionGetter.get(roleBaseAccountGenerator.getAddress());
        KlayRawTransaction klayRawTransaction = transactionManager.sign(transactionTransformer);

        // 2. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerCredentialsList.get(0))
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(klayRawTransaction.getValueAsString());
        receiptChecker.check(transactionTransformer, transactionReceipt);
    }

    protected interface TransactionGetter {
        TransactionTransformer get(String address) throws Exception;
    }

    protected interface ReceiptChecker {
        void check(TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) throws Exception;
    }
}

class AccountSizeGenerator {
    private int transactionAccountSize;
    private int updateAccountSize;
    private int feePayerAccountSize;

    public AccountSizeGenerator() {
        Random random = new Random();
        this.transactionAccountSize = random.nextInt(9) + 1;
        this.updateAccountSize = random.nextInt(9) + 1;
        this.feePayerAccountSize = random.nextInt(9) + 1;
    }

    public int getTransactionAccountSize() {
        return transactionAccountSize;
    }

    public int getUpdateAccountSize() {
        return updateAccountSize;
    }

    public int getFeePayerAccountSize() {
        return feePayerAccountSize;
    }
}