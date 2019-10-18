package com.klaytn.caver.scenario;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.methods.response.KlayAccountKey;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.account.AccountKey;
import com.klaytn.caver.tx.account.AccountKeyRoleBased;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.util.ArrayList;
import java.util.List;

import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static org.junit.Assert.assertEquals;

public class RoleBaseAccountGenerator extends Scenario {
    private List<KlayCredentials> transactionAccountCredential;
    private List<KlayCredentials> updateAccountCredential;
    private List<KlayCredentials> feePayerAccountCredential;
    private KlayCredentials oldAccount;
    private AccountKey newAccountKey;
    private String address;

    public RoleBaseAccountGenerator() throws Exception {
        transactionAccountCredential = new ArrayList<>();
        updateAccountCredential = new ArrayList<>();
        feePayerAccountCredential = new ArrayList<>();
        this.caver = Caver.build(Caver.DEFAULT_URL);
    }

    public List<KlayCredentials> getTransactionAccountCredential() {
        return transactionAccountCredential;
    }

    public List<KlayCredentials> getUpdateAccountCredential() {
        return updateAccountCredential;
    }

    public List<KlayCredentials> getFeePayerAccountCredential() {
        return feePayerAccountCredential;
    }

    public List<KlayCredentials> getSenderCredentialForTest(boolean isUpdateTest) {
        if (isUpdateTest) {
            return updateAccountCredential;
        }
        return transactionAccountCredential;
    }

    public KlayCredentials getOldAccount() {
        return oldAccount;
    }

    public AccountKey getNewAccountKey() {
        return newAccountKey;
    }

    public String getAddress() {
        return address;
    }

    public void initTestSet(int transactionAccountCount, int updateAccountCount, int feePayerAccountCount) throws Exception {
        oldAccount = createAccount();
        address = oldAccount.getAddress();

        setRandomRoleBasedNewAccountKey(oldAccount, transactionAccountCount, updateAccountCount, feePayerAccountCount);

        TransactionManager transactionManager = new TransactionManager.Builder(caver, oldAccount)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                oldAccount.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);
        assertEquals("0x1", transactionReceipt.getStatus());

        KlayAccountKey klayAccountKey = caver.klay().getAccountKey(oldAccount.getAddress(), DefaultBlockParameterName.LATEST).send();
        AccountKey responseAccountKey = klayAccountKey.getResult().getKey();

        assertEquals("Response\n" + responseAccountKey.toString() + "\nExpected" + newAccountKey.toString(), responseAccountKey, newAccountKey);
    }

    public void setRandomRoleBasedNewAccountKey(KlayCredentials oldAccount, int transactionAccountCount, int updateAcocountCount, int feePayerAccountCount) throws Exception {
        List<AccountKey> roleBasedAccountKeyList = new ArrayList<>();
        List<ECKeyPair> transactionECKeyPairList = new ArrayList<>();
        List<ECKeyPair> updateECKeyPairList = new ArrayList<>();
        List<ECKeyPair> feePayerECKeyPairList = new ArrayList<>();

        for (int i = 0; i < transactionAccountCount; i++) {
            KlayCredentials credentials = KlayCredentials.create(createECKeyPairList(10 / transactionAccountCount), createECKeyPairList(0), createECKeyPairList(0), oldAccount.getAddress());
            transactionAccountCredential.add(credentials);
            transactionECKeyPairList.addAll(credentials.getEcKeyPairsForTransactionList());
        }
        for (int i = 0; i < updateAcocountCount; i++) {
            KlayCredentials credentials = KlayCredentials.create(createECKeyPairList(0), createECKeyPairList(10 / updateAcocountCount), createECKeyPairList(0), oldAccount.getAddress());
            updateAccountCredential.add(credentials);
            updateECKeyPairList.addAll(credentials.getEcKeyPairsForUpdateList());
        }
        for (int i = 0; i < feePayerAccountCount; i++) {
            KlayCredentials credentials = KlayCredentials.create(createECKeyPairList(0), createECKeyPairList(0), createECKeyPairList(10 / feePayerAccountCount), oldAccount.getAddress());
            feePayerAccountCredential.add(credentials);
            feePayerECKeyPairList.addAll(credentials.getEcKeyPairsForFeePayerList());
        }

        roleBasedAccountKeyList.add(createRandomAccountKeyWeightedMultiSig(transactionECKeyPairList));
        roleBasedAccountKeyList.add(createRandomAccountKeyWeightedMultiSig(updateECKeyPairList));
        roleBasedAccountKeyList.add(createRandomAccountKeyWeightedMultiSig(feePayerECKeyPairList));

        newAccountKey = AccountKeyRoleBased.create(roleBasedAccountKeyList);
    }
}
