package com.klaytn.caver.scenario;

import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.model.SmartContractDeployTransaction;
import com.klaytn.caver.tx.model.TransactionTransformer;
import com.klaytn.caver.utils.CodeFormat;
import org.junit.Test;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class RoleBasedSmartContractDeployIT extends RoleBasedIT {
    final String CONTRACT_INPUT = "0x60806040526000805534801561001457600080fd5b50610116806100246000396000f3006080604052600436106053576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306661abd14605857806342cbb15c146080578063d14e62b81460a8575b600080fd5b348015606357600080fd5b50606a60d2565b6040518082815260200191505060405180910390f35b348015608b57600080fd5b50609260d8565b6040518082815260200191505060405180910390f35b34801560b357600080fd5b5060d06004803603810190808035906020019092919050505060e0565b005b60005481565b600043905090565b80600081905550505600a165627a7a7230582064856de85a2706463526593b08dd790054536042ef66d3204018e6790a2208d10029";
    //////////////////////////////// BasicTest ////////////////////////////////
    @Test
    public void smartContractDeployTest() throws Exception{
        roleBasedTransactionTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }
    //////////////////////////////// FeeDelegateTest ////////////////////////////////
    @Test
    public void feeDelegatedSmartContractDeployTest() throws Exception{
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedSmartContractDeployWithRatio() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexStringWithPrefix(feeRatio), transactionReceipt.getFeeRatio());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }
    //////////////////////////////// MultiTransactionSignerTest ////////////////////////////////
    @Test
    public void feeDelegatedSmartContractDeployMultiTransactionSignerTest() throws Exception{
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedSmartContractDeployWithRatioMultiTransactionSignerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedMultiTransactionSignerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexStringWithPrefix(feeRatio), transactionReceipt.getFeeRatio());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());

                }, false);
    }

    //////////////////////////////// MultiFeePayerTest ////////////////////////////////
    @Test
    public void feeDelegatedSmartContractDeployMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());

                }, false);
    }

    @Test
    public void feeDelegatedSmartContractDeployWithRatioMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiFeePayerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexStringWithPrefix(feeRatio), transactionReceipt.getFeeRatio());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());

                }, false);
    }

    //////////////////////////////// MultiSignerMultiFeePayerTest ////////////////////////////////
    @Test
    public void feeDelegatedSmartContractDeployMultiTransactionSignerMultiFeePayerTest() throws Exception{
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate(),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }

    @Test
    public void feeDelegatedSmartContractDeployWithRatioMultiTransactionSignerMultiFeePayerTest() throws Exception{
        Random random = new Random();
        BigInteger feeRatio = BigInteger.valueOf(random.nextInt(99) + 1);
        feeDelegatedRoleBasedTransactionMultiTransactionSignerMultiFeePayerTest(
                (String address) -> SmartContractDeployTransaction.create(
                        address,
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(CONTRACT_INPUT),
                        GAS_LIMIT,
                        CodeFormat.EVM
                ).feeDelegate().feeRatio(feeRatio),
                (TransactionTransformer transactionTransformer, KlayTransactionReceipt.TransactionReceipt transactionReceipt) ->{
                    assertEquals("0x1", transactionReceipt.getStatus());
                    assertEquals(Numeric.toHexStringWithPrefix(feeRatio), transactionReceipt.getFeeRatio());
                    assertEquals(CONTRACT_INPUT, transactionReceipt.getInput());
                }, false);
    }

}
