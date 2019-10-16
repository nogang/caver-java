package com.klaytn.caver.scenario;

import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.SmartContract;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.KlayRawTransaction;
import com.klaytn.caver.tx.model.SmartContractDeployTransaction;
import com.klaytn.caver.tx.model.SmartContractExecutionTransaction;
import com.klaytn.caver.tx.model.ValueTransferTransaction;
import com.klaytn.caver.utils.CodeFormat;
import org.junit.Test;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static com.klaytn.caver.base.Accounts.BRANDON;
import static com.klaytn.caver.base.Accounts.LUMAN;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static org.junit.Assert.assertEquals;

public class SmartContractIT extends Scenario {
    /*
    @Test
    public void testSmartContractDeploy() {
        String contractInput = "0x60806040526000805534801561001457600080fd5b50610116806100246000396000f3006080604052600436106053576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306661abd14605857806342cbb15c146080578063d14e62b81460a8575b600080fd5b348015606357600080fd5b50606a60d2565b6040518082815260200191505060405180910390f35b348015608b57600080fd5b50609260d8565b6040518082815260200191505060405180910390f35b34801560b357600080fd5b5060d06004803603810190808035906020019092919050505060e0565b005b60005481565b600043905090565b80600081905550505600a165627a7a7230582064856de85a2706463526593b08dd790054536042ef66d3204018e6790a2208d10029";

        SmartContractDeployTransaction smartContractDeployTransaction = SmartContractDeployTransaction.create(
                LUMAN.getAddress(),
                BigInteger.ZERO,
                Numeric.hexStringToByteArray(contractInput),
                GAS_LIMIT,
                CodeFormat.EVM
        );

        KlayTransactionReceipt.TransactionReceipt contractDeployReceipt = transactionManager.executeTransaction(smartContractDeployTransaction);
        assertEquals("0x1", contractDeployReceipt.getStatus());
    }
*/

    @Test
    public void transferMultiTransactionSignerTest() throws Exception{
        int transactionAccountSize = 10;
        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(transactionAccountSize,1,1);
        List<KlayCredentials> transactionCredentialsList = roleBaseAccountGenerator.getTransactionAccountCredential();

        KlayRawTransaction klayRawTransaction = null;
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = null;

        for (int i = 0 ; i < transactionAccountSize; i++) {
            TransactionManager transactionManager = new TransactionManager.Builder(caver, transactionCredentialsList.get(i))
                    .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                    .setChaindId(LOCAL_CHAIN_ID)
                    .build();

            if (klayRawTransaction == null) {
                // 1. The transaction constructor creates and signs a transaction.
                String contractInput = "0x60806040526000805534801561001457600080fd5b50610116806100246000396000f3006080604052600436106053576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306661abd14605857806342cbb15c146080578063d14e62b81460a8575b600080fd5b348015606357600080fd5b50606a60d2565b6040518082815260200191505060405180910390f35b348015608b57600080fd5b50609260d8565b6040518082815260200191505060405180910390f35b34801560b357600080fd5b5060d06004803603810190808035906020019092919050505060e0565b005b60005481565b600043905090565b80600081905550505600a165627a7a7230582064856de85a2706463526593b08dd790054536042ef66d3204018e6790a2208d10029";

                SmartContractDeployTransaction valueTransferTransaction = SmartContractDeployTransaction.create(
                        transactionCredentialsList.get(i).getAddress(),
                        BigInteger.ZERO,
                        Numeric.hexStringToByteArray(contractInput),
                        GAS_LIMIT,
                        CodeFormat.EVM
                );
                //ValueTransferTransaction valueTransferTransaction = ValueTransferTransaction.create(roleBaseAccountGenerator.getAddress(), BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT);
                klayRawTransaction = transactionManager.sign(valueTransferTransaction);
            } else if (i < transactionAccountSize - 1){
                // 2. Those with the RoleTransaction key receive and sign the rawTransaction.
                klayRawTransaction = transactionManager.sign(klayRawTransaction.getValueAsString());
            } else {
                // 3. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
                transactionReceipt = transactionManager.executeTransaction(klayRawTransaction.getValueAsString());
            }
        }

        assertEquals("0x1", transactionReceipt.getStatus());
    }

    @Test
    public void testSmartContractExecution() throws Exception {

        int transactionAccountSize = 10;
        RoleBaseAccountGenerator roleBaseAccountGenerator = new RoleBaseAccountGenerator();
        roleBaseAccountGenerator.initTestSet(transactionAccountSize,1,1);
        List<KlayCredentials> transactionCredentialsList = roleBaseAccountGenerator.getTransactionAccountCredential();

        KlayRawTransaction klayRawTransaction = null;
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = null;

        for (int i = 0 ; i < transactionAccountSize; i++) {
            TransactionManager transactionManager = new TransactionManager.Builder(caver, transactionCredentialsList.get(i))
                    .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                    .setChaindId(LOCAL_CHAIN_ID)
                    .build();

            if (klayRawTransaction == null) {
                // 1. The transaction constructor creates and signs a transaction.
                String deployedContractAddress = getDeployedContract();

                SmartContractExecutionTransaction valueTransferTransaction = SmartContractExecutionTransaction.create(
                        transactionCredentialsList.get(i).getAddress(),
                        deployedContractAddress,
                        BigInteger.ZERO,
                        getChangePayload(),
                        GAS_LIMIT
                );
                //ValueTransferTransaction valueTransferTransaction = ValueTransferTransaction.create(roleBaseAccountGenerator.getAddress(), BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT);
                klayRawTransaction = transactionManager.sign(valueTransferTransaction);
            } else if (i < transactionAccountSize - 1){
                // 2. Those with the RoleTransaction key receive and sign the rawTransaction.
                klayRawTransaction = transactionManager.sign(klayRawTransaction.getValueAsString());
            } else {
                // 3. After all signs are signed, the last person receiving the transaction signs and transmits the transaction to the klaytn network.
                transactionReceipt = transactionManager.executeTransaction(klayRawTransaction.getValueAsString());
            }
        }

        assertEquals("0x1", transactionReceipt.getStatus());
    }

    private String getDeployedContract() throws Exception {
        byte[] payload = Numeric.hexStringToByteArray("0x60806040526000805534801561001457600080fd5b50610116806100246000396000f3006080604052600436106053576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306661abd14605857806342cbb15c146080578063d14e62b81460a8575b600080fd5b348015606357600080fd5b50606a60d2565b6040518082815260200191505060405180910390f35b348015608b57600080fd5b50609260d8565b6040518082815260200191505060405180910390f35b34801560b357600080fd5b5060d06004803603810190808035906020019092919050505060e0565b005b60005481565b600043905090565b80600081905550505600a165627a7a7230582064856de85a2706463526593b08dd790054536042ef66d3204018e6790a2208d10029");
        SmartContract smartContract = SmartContract.create(caver, new TransactionManager.Builder(caver, BRANDON).setChaindId(LOCAL_CHAIN_ID).build());
        KlayTransactionReceipt.TransactionReceipt receipt = smartContract.sendDeployTransaction(SmartContractDeployTransaction.create(
                BRANDON.getAddress(), BigInteger.ZERO, payload, GAS_LIMIT, CodeFormat.EVM
        )).send();
        return receipt.getContractAddress();
    }

    private byte[] getChangePayload() {
        String setCommand = "setCount(uint256)";
        int changeValue = 27;

        BigInteger replaceValue = BigInteger.valueOf(changeValue);
        String payLoadNoCommand = Numeric.toHexStringNoPrefix(Numeric.toBytesPadded(replaceValue, 32));
        String payLoad = Hash.sha3String(setCommand).substring(2, 10) + payLoadNoCommand;
        return Numeric.hexStringToByteArray(payLoad);
    }
}
