package com.klaytn.caver.scenario;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.methods.request.CallObject;
import com.klaytn.caver.methods.response.Bytes;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.SmartContract;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.TransactionTransformer;
import com.klaytn.caver.tx.type.TxTypeFeeDelegate;
import com.klaytn.caver.utils.CodeFormat;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Random;

import static com.klaytn.caver.base.Accounts.BRANDON;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static org.junit.Assert.assertEquals;

public class RoleBasedUpdateAccountIT extends RoleBasedIT {

    private final AccountUpdateTransaction getAccountUpdateTransactionTransformer(String from) throws Exception {
        KlayCredentials oldAccount = createAccount();

        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.setRandomRoleBasedNewAccountKey(oldAccount, 10,10,10);

        return AccountUpdateTransaction.create(
                from,
                roleBaseAccountGenerator.getNewAccountKey(),
                GAS_LIMIT
        );
    }

    private final TransactionGetter getAccountUpdateTransactionGetter = (String from) -> getAccountUpdateTransactionTransformer(from);

    private final TransactionGetter getFeeDelegatedAccountUpdateTransactionGetter = (String from) -> getAccountUpdateTransactionTransformer(from).feeDelegate();

    private final TransactionGetter getFeeRatioAccountUpdateTransactionGetter = (String from) -> {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        return getAccountUpdateTransactionTransformer(from).feeDelegate().feeRatio(feeRatio);
    };

    private final ReceiptChecker getAccountUpdateReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());
    };

    private final ReceiptChecker getFeeDelegatedAccountUpdateReceiptChecker = getAccountUpdateReceiptChecker;
    private final ReceiptChecker getFeeRatioAccountUpdateReceiptChecker = (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) -> {
        assertEquals("0x1", transactionReceipt.getStatus());

        TxTypeFeeDelegate txTypeFeeDelegate = (TxTypeFeeDelegate) transactionTransformer.build();
        assertEquals(Numeric.toHexStringWithPrefix(txTypeFeeDelegate.getFeeRatio()), transactionReceipt.getFeeRatio());
    };

    //////////////////////////////// BasicTest ////////////////////////////////
    @Before
    public void setUp() {
        caver = Caver.build(Caver.DEFAULT_URL);
    }

    @Test
    public void AccountUpdateTest() throws Exception {
        roleBasedTransactionTest(
                getAccountUpdateTransactionGetter,
                getAccountUpdateReceiptChecker, true);
    }

    //////////////////////////////// FeeDelegateTest ////////////////////////////////
    @Test
    public void feeDelegatedAccountUpdateTest() throws Exception {
        feeDelegatedRoleBasedTransactionTest(
                getFeeDelegatedAccountUpdateTransactionGetter,
                getFeeDelegatedAccountUpdateReceiptChecker
        , true);
    }

    @Test
    public void feeDelegatedAccountUpdateWithRatio() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                getFeeRatioAccountUpdateTransactionGetter,
                getFeeRatioAccountUpdateReceiptChecker
        , true);
    }

    //////////////////////////////// MultiTransactionSignerTest ////////////////////////////////
    @Test
    public void feeDelegatedAccountUpdateMultiTransactionSignerTest() throws Exception {
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                getFeeDelegatedAccountUpdateTransactionGetter,
                getFeeDelegatedAccountUpdateReceiptChecker
                , true);
    }

    @Test
    public void feeDelegatedAccountUpdateWithRatioMultiTransactionSignerTest() throws Exception {
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                getFeeRatioAccountUpdateTransactionGetter,
                getFeeRatioAccountUpdateReceiptChecker
        , true);
    }

    //////////////////////////////// MultiFeePayerTest ////////////////////////////////
    @Test
    public void feeDelegatedAccountUpdateMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                getFeeDelegatedAccountUpdateTransactionGetter,
                getFeeDelegatedAccountUpdateReceiptChecker
        , true);
    }

    @Test
    public void feeDelegatedAccountUpdateWithRatioMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                getFeeRatioAccountUpdateTransactionGetter,
                getFeeRatioAccountUpdateReceiptChecker
        , true);
    }

    //////////////////////////////// MultiSignerMultiFeePayerTest ////////////////////////////////
    @Test
    public void feeDelegatedAccountUpdateMultiTransactionSignerMultiFeePayerTest() throws Exception {
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                getFeeDelegatedAccountUpdateTransactionGetter,
                getFeeDelegatedAccountUpdateReceiptChecker
        , true);
    }

    @Test
    public void feeDelegatedAccountUpdateWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception {
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(100));
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                getFeeRatioAccountUpdateTransactionGetter,
                getFeeRatioAccountUpdateReceiptChecker
        , true);
    }
}

