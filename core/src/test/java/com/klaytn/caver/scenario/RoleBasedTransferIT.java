package com.klaytn.caver.scenario;

import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.model.TransactionTransformer;
import com.klaytn.caver.tx.model.ValueTransferTransaction;
import org.junit.Test;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Random;

import static com.klaytn.caver.base.Accounts.BRANDON;
import static org.junit.Assert.assertEquals;

public class RoleBasedTransferIT extends RoleBasedIT {
    //////////////////////////////// BasicTest - value Transfer ////////////////////////////////
    @Test
    public void valueTransferTest() throws Exception{
        roleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                }, false);
    }
    @Test
    public void valueTransferMemoTest() throws Exception{
        roleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).memo(MEMO),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                }, false);
    }

    //////////////////////////////// FeeDelegateTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferTest() throws Exception{
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferMemoTest() throws Exception{
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferWithRatio() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
    //////////////////////////////// MultiTransactionSignerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiTransactionSignerTest() throws Exception{
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferMemoMultiTransactionSignerTest() throws Exception{
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiTransactionSignerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferWithRatioMultiTransactionSignerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }

    //////////////////////////////// MultiFeePayerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferMemoMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferWithRatioMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }

    //////////////////////////////// MultiSignerMultiFeePayerTest - value Transfer ////////////////////////////////
    @Test
    public void feeDelegatedValueTransferMultiTransactionSignerMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferMemoMultiTransactionSignerMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedValueTransferMemoWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().memo(MEMO).feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexString(MEMO.getBytes()), transactionReceipt.getInput());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
    @Test
    public void feeDelegatedValueTransferWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> ValueTransferTransaction.create(address, BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals("0x" + Numeric.toHexStringNoPrefix(feeRatio), transactionReceipt.getFeeRatio());
                }, false);
    }
}
