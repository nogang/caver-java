package com.klaytn.caver.scenario;

import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.fee.FeePayerManager;
import com.klaytn.caver.methods.response.KlayAccountKey;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.account.*;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.KlayRawTransaction;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UpdateAccountIT extends Scenario {

    @Test
    public void testUpdatePublicAccountKey() throws Exception {
        KlayCredentials oldAccount = createAccount();
        KlayCredentials newAccount = createAccount(oldAccount);

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountKey newAccountKey = AccountKeyPublic.create(newAccount.getEcKeyPair().getPublicKey());
        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals(true, responseAccountKey.equals(newAccountKey));
    }

    @Test
    public void testUpdatePublicAccountKeyWithFeeDelegate() throws Exception {
        KlayCredentials oldAccount = createAccount();
        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        KlayCredentials newAccount = createAccount(oldAccount);
        AccountKey newAccountKey = AccountKeyPublic.create(newAccount.getEcKeyPair().getPublicKey());
        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();
        KlayRawTransaction rawAccountUpdateTx = transactionManager.sign(accountUpdateTx);

        KlayCredentials feePayerAccount = createAccount();
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(rawAccountUpdateTx.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals(true, responseAccountKey.equals(newAccountKey));
    }

    @Test
    public void testUpdateMultiSigAccountKey() throws Exception {
        KlayCredentials oldAccount = createAccount();
        Random random = new Random();

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        List<AccountKeyWeightedMultiSig.WeightedPublicKey> weightedTransactionPublicKeys = new ArrayList<>();

        int sumOfWeight = 0;
        for (int i = 0 ; i < random.nextInt(10) + 1 ; i++) {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            int weight = random.nextInt(20) + 1;
            sumOfWeight += weight;
            AccountKeyWeightedMultiSig.WeightedPublicKey key = AccountKeyWeightedMultiSig.WeightedPublicKey.create(
                    BigInteger.valueOf(weight),
                    AccountKeyPublic.create(ecKeyPair.getPublicKey())
            );
            weightedTransactionPublicKeys.add(key);
        }

        AccountKey newAccountKey = AccountKeyWeightedMultiSig.create(
                BigInteger.valueOf(random.nextInt(sumOfWeight) + 1),
                weightedTransactionPublicKeys
        );

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals(true, responseAccountKey.equals(newAccountKey));
    }

    @Test
    public void testUpdateMultiSigAccountKeyWithFeeDelegate() throws Exception {
        KlayCredentials oldAccount = createAccount();
        Random random = new Random();

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountKey newAccountKey = createRandomAccountKeyWeightedMultiSig();

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction rawAccountUpdateTx = transactionManager.sign(accountUpdateTx);
        KlayCredentials feePayerAccount = createAccount();
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(rawAccountUpdateTx.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals(responseAccountKey.toString()+"\n"+newAccountKey.toString(),responseAccountKey, newAccountKey);
    }

    @Test
    public void testtest() throws  Exception{
        for (int i = 0 ; i < 10 ; i++) {
            //testUpdateRoleBasedAccountKeyUsingRoleBased();
        }
    }

    @Test
    public void testUpdateRoleBasedAccountKey() throws Exception {
        KlayCredentials oldAccount = createAccount();

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountKey newAccountKey =  createRandomAccountKeyRoleBased();

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals(responseAccountKey.toString()+"\n"+newAccountKey.toString(),responseAccountKey, newAccountKey);
    }

    @Test
    public void testUpdateRoleBasedAccountKeyWithFeeDelegate() throws Exception {
        KlayCredentials oldAccount = createAccount();

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountKey newAccountKey = createRandomAccountKeyWeightedMultiSig();

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction rawAccountUpdateTx = transactionManager.sign(accountUpdateTx);
        KlayCredentials feePayerAccount = createAccount();
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(rawAccountUpdateTx.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    @Test
    public void testUpdateRoleBasedAccountKeyUsingRoleBased() throws Exception {
        KlayCredentials oldAccount = createAccount();
        KlayCredentials transactionAccount = KlayCredentials.create(createECKeyPairList(10),createECKeyPairList(0),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials updateAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(10),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials feePayerAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(0),createECKeyPairList(10), oldAccount.getAddress());

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        // 1. update account key to roleBasedAccountKey
        List<AccountKey> roleBasedAccountKeyList = new ArrayList<>();

        AccountKeyWeightedMultiSig accountKeyForTransaction = createRandomAccountKeyWeightedMultiSig(transactionAccount.getEcKeyPairsForTransactionList());
        roleBasedAccountKeyList.add(accountKeyForTransaction);
        AccountKeyWeightedMultiSig accountKeyForUpdate = createRandomAccountKeyWeightedMultiSig(updateAccount.getEcKeyPairsForUpdateList());
        roleBasedAccountKeyList.add(accountKeyForUpdate);
        AccountKeyWeightedMultiSig accountKeyForFeeDelegate = createRandomAccountKeyWeightedMultiSig(feePayerAccount.getEcKeyPairsForFeePayerList());
        roleBasedAccountKeyList.add(accountKeyForFeeDelegate);

        AccountKey newAccountKey = AccountKeyRoleBased.create(roleBasedAccountKeyList);

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);

        // 2. update using roleUpdate
        TransactionManager transactionManagerOfUpdateAccount = new TransactionManager.Builder(caver, updateAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        newAccountKey = createRandomAccountKeyRoleBased();
        accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        );

        transactionReceipt = transactionManagerOfUpdateAccount.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    @Test
    public void testUpdateRoleBasedAccountKeyUsingRoleBasedWithFeeDelegate() throws Exception {
        KlayCredentials oldAccount = createAccount();
        KlayCredentials transactionAccount = KlayCredentials.create(createECKeyPairList(10),createECKeyPairList(0),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials updateAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(10),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials feePayerAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(0),createECKeyPairList(10), oldAccount.getAddress());

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        // 1. update account key to roleBasedAccountKey
        List<AccountKey> roleBasedAccountKeyList = new ArrayList<>();

        AccountKeyWeightedMultiSig accountKeyForTransaction = createRandomAccountKeyWeightedMultiSig(transactionAccount.getEcKeyPairsForTransactionList());
        roleBasedAccountKeyList.add(accountKeyForTransaction);
        AccountKeyWeightedMultiSig accountKeyForUpdate = createRandomAccountKeyWeightedMultiSig(updateAccount.getEcKeyPairsForUpdateList());
        roleBasedAccountKeyList.add(accountKeyForUpdate);
        AccountKeyWeightedMultiSig accountKeyForFeeDelegate = createRandomAccountKeyWeightedMultiSig(feePayerAccount.getEcKeyPairsForFeePayerList());
        roleBasedAccountKeyList.add(accountKeyForFeeDelegate);

        AccountKey newAccountKey = AccountKeyRoleBased.create(roleBasedAccountKeyList);

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);

        // 2. update using roleUpdate and feeDelegate
        TransactionManager transactionManagerOfUpdateAccount = new TransactionManager.Builder(caver, updateAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        //newAccountKey = createRandomAccountKeyRoleBased();
        accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction rawAccountUpdateTx = transactionManagerOfUpdateAccount.sign(accountUpdateTx);

        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        transactionReceipt = feePayerManager.executeTransaction(rawAccountUpdateTx.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    @Test
    public void testUpdateRoleBasedAccountKeyUsingRoleBasedWithFeeDelegateTwoFeePayer() throws Exception {
        KlayCredentials oldAccount = createAccount();
        KlayCredentials transactionAccount = KlayCredentials.create(createECKeyPairList(10),createECKeyPairList(0),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials updateAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(10),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials feePayerAccount1 = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(0),createECKeyPairList(5), oldAccount.getAddress());
        KlayCredentials feePayerAccount2 = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(0),createECKeyPairList(5), oldAccount.getAddress());

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        // 1. update account key to roleBasedAccountKey
        List<AccountKey> roleBasedAccountKeyList = new ArrayList<>();

        List<ECKeyPair> feePayerECKeyPairList = new ArrayList<>();
        feePayerECKeyPairList.addAll(feePayerAccount1.getEcKeyPairsForFeePayerList());
        feePayerECKeyPairList.addAll(feePayerAccount2.getEcKeyPairsForFeePayerList());
        AccountKeyWeightedMultiSig accountKeyForTransaction = createRandomAccountKeyWeightedMultiSig(transactionAccount.getEcKeyPairsForTransactionList());
        roleBasedAccountKeyList.add(accountKeyForTransaction);
        AccountKeyWeightedMultiSig accountKeyForUpdate = createRandomAccountKeyWeightedMultiSig(updateAccount.getEcKeyPairsForUpdateList());
        roleBasedAccountKeyList.add(accountKeyForUpdate);
        AccountKeyWeightedMultiSig accountKeyForFeeDelegate = createRandomAccountKeyWeightedMultiSig(feePayerECKeyPairList);
        roleBasedAccountKeyList.add(accountKeyForFeeDelegate);

        AccountKey newAccountKey = AccountKeyRoleBased.create(roleBasedAccountKeyList);

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);

        // 2. update using roleUpdate and feeDelegate
        TransactionManager transactionManagerOfUpdateAccount = new TransactionManager.Builder(caver, updateAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        //newAccountKey = createRandomAccountKeyRoleBased();
        accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction rawAccountUpdateTx = transactionManagerOfUpdateAccount.sign(accountUpdateTx);

        FeePayerManager feePayerManager1 = new FeePayerManager.Builder(caver, feePayerAccount1)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayRawTransaction rawTransaction = feePayerManager1.sign(rawAccountUpdateTx.getValueAsString());

        FeePayerManager feePayerManager2 = new FeePayerManager.Builder(caver, feePayerAccount2)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        transactionReceipt = feePayerManager2.executeTransaction(rawTransaction.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    @Test
    public void testUpdateRoleBasedAccountKeyUsingRoleBasedWithFeeDelegateTwoUpdate() throws Exception {
        KlayCredentials oldAccount = createAccount();
        KlayCredentials transactionAccount = KlayCredentials.create(createECKeyPairList(10),createECKeyPairList(0),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials updateAccount1 = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(5),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials updateAccount2 = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(5),createECKeyPairList(0), oldAccount.getAddress());
        KlayCredentials feePayerAccount = KlayCredentials.create(createECKeyPairList(0),createECKeyPairList(0),createECKeyPairList(10), oldAccount.getAddress());

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        // 1. update account key to roleBasedAccountKey
        List<AccountKey> roleBasedAccountKeyList = new ArrayList<>();

        List<ECKeyPair> updateECKeyPairList = new ArrayList<>();
        updateECKeyPairList.addAll(updateAccount1.getEcKeyPairsForUpdateList());
        updateECKeyPairList.addAll(updateAccount2.getEcKeyPairsForUpdateList());
        AccountKeyWeightedMultiSig accountKeyForTransaction = createRandomAccountKeyWeightedMultiSig(transactionAccount.getEcKeyPairsForTransactionList());
        roleBasedAccountKeyList.add(accountKeyForTransaction);
        AccountKeyWeightedMultiSig accountKeyForUpdate = createRandomAccountKeyWeightedMultiSig(updateECKeyPairList);
        roleBasedAccountKeyList.add(accountKeyForUpdate);
        AccountKeyWeightedMultiSig accountKeyForFeeDelegate = createRandomAccountKeyWeightedMultiSig(feePayerAccount.getEcKeyPairsForFeePayerList());
        roleBasedAccountKeyList.add(accountKeyForFeeDelegate);

        AccountKey newAccountKey = AccountKeyRoleBased.create(roleBasedAccountKeyList);

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);

        // 2. update using roleUpdate and feeDelegate
        TransactionManager transactionManagerOfUpdateAccount1 = new TransactionManager.Builder(caver, updateAccount1)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        TransactionManager transactionManagerOfUpdateAccount2 = new TransactionManager.Builder(caver, updateAccount2)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();


        //newAccountKey = createRandomAccountKeyRoleBased();
        accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(), // from
                newAccountKey,
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction rawAccountUpdateTx1 = transactionManagerOfUpdateAccount1.sign(accountUpdateTx);
        KlayRawTransaction rawAccountUpdateTx2 = transactionManagerOfUpdateAccount2.sign(rawAccountUpdateTx1.getValueAsString());

        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, feePayerAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        transactionReceipt = feePayerManager.executeTransaction(rawAccountUpdateTx2.getValueAsString());
        assertEquals("0x1", transactionReceipt.getStatus());

        klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        responseAccountKey = klayAccountKey.getResult().getKey();
        assertEquals("Response\n" + responseAccountKey.toString()+ "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    public AccountKeyRoleBased createRandomAccountKeyRoleBased() throws Exception{
        Random random = new Random();
        List<AccountKey> accountKeyList = new ArrayList<>();

        for (int i=0 ; i < 3 ; i++) {
            accountKeyList.add(random.nextInt(2) == 0 ? AccountKeyPublic.create(Keys.createEcKeyPair().getPublicKey()) : createRandomAccountKeyWeightedMultiSig());
        }

        return AccountKeyRoleBased.create(accountKeyList);
    }

    private AccountKeyWeightedMultiSig createRandomAccountKeyWeightedMultiSig() throws Exception{
        Random random = new Random();
        List<AccountKeyWeightedMultiSig.WeightedPublicKey> weightedTransactionPublicKeys = new ArrayList<>();
        int sumOfWeight = 0;
        for (int i = 0 ; i < random.nextInt(10) + 1 ; i++) {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            int weight = random.nextInt(20) + 1;
            sumOfWeight += weight;
            AccountKeyWeightedMultiSig.WeightedPublicKey key = AccountKeyWeightedMultiSig.WeightedPublicKey.create(
                    BigInteger.valueOf(weight),
                    AccountKeyPublic.create(ecKeyPair.getPublicKey())
            );
            weightedTransactionPublicKeys.add(key);
        }

        AccountKeyWeightedMultiSig newAccountKey = AccountKeyWeightedMultiSig.create(
                BigInteger.valueOf(random.nextInt(sumOfWeight) + 1),
                weightedTransactionPublicKeys
        );

        return newAccountKey;
    }



}
