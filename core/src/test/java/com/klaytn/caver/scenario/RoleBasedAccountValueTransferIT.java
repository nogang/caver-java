package com.klaytn.caver.scenario;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.ValueTransfer;
import com.klaytn.caver.tx.account.AccountKeyPublic;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.TransactionTransformer;
import com.klaytn.caver.tx.model.ValueTransferTransaction;
import com.klaytn.caver.tx.type.TxTypeFeeDelegate;
import com.klaytn.caver.utils.Convert;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Random;

import static com.klaytn.caver.base.Accounts.BRANDON;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static org.junit.Assert.assertEquals;

public class RoleBasedAccountValueTransferIT extends RoleBasedAccountScenario {

    private final ReceiptChecker getReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());
    };

    private final ReceiptChecker getFeeDelegatedValueTransferMemoWithRatioReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());
        assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
        TxTypeFeeDelegate txTypeFeeDelegate = (TxTypeFeeDelegate) transactionTransformer.build();
        assertEquals(Numeric.toHexStringWithPrefix(txTypeFeeDelegate.getFeeRatio()), transactionReceipt.getFeeRatio());
    };

    private final ReceiptChecker getFeeDelegatedValueTransferWithRatioReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());
        TxTypeFeeDelegate txTypeFeeDelegate = (TxTypeFeeDelegate) transactionTransformer.build();
        assertEquals(Numeric.toHexStringWithPrefix(txTypeFeeDelegate.getFeeRatio()), transactionReceipt.getFeeRatio());
    };

    private final ReceiptChecker getFeeDelegatedValueTransferMemoReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());
        assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
    };

    private final ValueTransferTransaction getValueTransferTransaction(String from) {
        return ValueTransferTransaction.create(from, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT);
    }

    //////////////////////////////// BasicTest - value Transfer ////////////////////////////////
    @Test
    public void valueTransferTest() throws Exception {
        roleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from),
                getReceiptChecker, false);
    }

    @Test
    public void valueTransferMemoTest() throws Exception {
        roleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from).memo(MEMO),
                getFeeDelegatedValueTransferMemoReceiptChecker, false);
    }

    //////////////////////////////// FeeDelegateTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferTest() throws Exception {
        feeDelegatedRoleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate(),
                getReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoTest() throws Exception {
        feeDelegatedRoleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO),
                getFeeDelegatedValueTransferMemoReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioTest() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                getFeeDelegatedValueTransferMemoWithRatioReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferWithRatio() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().feeRatio(feeRatio),
                getFeeDelegatedValueTransferWithRatioReceiptChecker, false);
    }

    //////////////////////////////// MultiTransactionSignerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiTransactionSignerTest() throws Exception {
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate(),
                getReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoMultiTransactionSignerTest() throws Exception {
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO),
                getFeeDelegatedValueTransferMemoReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiTransactionSignerTest() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                getFeeDelegatedValueTransferMemoWithRatioReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferWithRatioMultiTransactionSignerTest() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().feeRatio(feeRatio),
                getFeeDelegatedValueTransferWithRatioReceiptChecker, false);
    }

    //////////////////////////////// MultiFeePayerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate(),
                getReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO),
                getFeeDelegatedValueTransferMemoReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiFeePayerTest() throws Exception {
        BigInteger feeRatio = BigInteger.valueOf(new Random().nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                getFeeDelegatedValueTransferMemoWithRatioReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferWithRatioMultiFeePayerTest() throws Exception {
        BigInteger feeRatio = BigInteger.valueOf(new Random().nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().feeRatio(feeRatio),
                getFeeDelegatedValueTransferWithRatioReceiptChecker, false);
    }

    //////////////////////////////// MultiSignerMultiFeePayerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiTransactionSignerMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate(),
                getReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoMultiTransactionSignerMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO),
                getFeeDelegatedValueTransferMemoReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception {
        BigInteger feeRatio = BigInteger.valueOf(new Random().nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                getFeeDelegatedValueTransferMemoWithRatioReceiptChecker, false);
    }

    @Test
    public void feeDelegatedValueTransferWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception {
        BigInteger feeRatio = BigInteger.valueOf(new Random().nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String from) -> getValueTransferTransaction(from).feeDelegate().feeRatio(feeRatio),
                getFeeDelegatedValueTransferWithRatioReceiptChecker, false);
    }
    @Test
    public void test() throws Exception {
        for (int i = 0; i <10 ;i++) {
            updateAccountToPublic();
        }

    }
    @Test
    public void updateAccountToPublic() throws Exception {

        //Caver caver = Caver.build(Caver.DEFAULT_URL);

        //1. PublicAccountKey로 업데이트 할 계정이 있어야 한다.
        //1-1. 계정 생성
        KlayCredentials originalCredential = KlayCredentials.create(Keys.createEcKeyPair());
        chargeAccount(originalCredential.getAddress());
        //1-2. credentials.getAddress() 에 해당하는 주소로 klay 충전.
        //2. AccountKey 생성
        //2-1. 새로 사용할 account key 생성 절대 이 키를 잃어버리면 안된다. 앞으로 사용될 키이기 때문에 이키를 보관하고 있어야 한다.
        ECKeyPair newKeyPair = Keys.createEcKeyPair();
        AccountKeyPublic newAccountKey = AccountKeyPublic.create(newKeyPair.getPublicKey());

        TransactionManager transactionManager = new TransactionManager.Builder(caver, originalCredential)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        AccountUpdateTransaction accountUpdateTx = AccountUpdateTransaction.create(
                originalCredential.getAddress(),
                newAccountKey,
                GAS_LIMIT
        );

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = transactionManager.executeTransaction(accountUpdateTx);

        // 새로운 키를 사용하여 사인할 수 있는 credentials를 만든다. 경고 privatekey와 address의 연결이 끊어졌으므로 credential을 생성할때 address를 입력하여야 한다.
        KlayCredentials newCredential = KlayCredentials.create(newKeyPair);

        TransactionManager newTransactionManager = new TransactionManager.Builder(caver, newCredential)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        ValueTransferTransaction valueTransferTransaction = ValueTransferTransaction.create(
                originalCredential.getAddress(),
                BRANDON.getAddress(),
                Convert.toPeb("100", Convert.Unit.KLAY).toBigInteger(),
                GAS_LIMIT
        );

        transactionReceipt = newTransactionManager.executeTransaction(valueTransferTransaction);
    }
}
