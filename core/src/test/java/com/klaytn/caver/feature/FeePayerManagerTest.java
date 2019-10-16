/*
 * Copyright 2019 The caver-java Authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klaytn.caver.feature;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.crypto.KlaySignatureData;
import com.klaytn.caver.fee.FeePayerManager;
import com.klaytn.caver.fee.FeePayerTransactionDecoder;
import com.klaytn.caver.methods.response.KlayAccountKey;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.ValueTransfer;
import com.klaytn.caver.tx.account.*;
import com.klaytn.caver.tx.manager.PollingTransactionReceiptProcessor;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.AccountUpdateTransaction;
import com.klaytn.caver.tx.model.KlayRawTransaction;
import com.klaytn.caver.tx.model.ValueTransferTransaction;
import com.klaytn.caver.tx.type.AbstractTxType;
import com.klaytn.caver.tx.type.TxType;
import com.klaytn.caver.utils.Convert;
import com.klaytn.caver.utils.TransactionDecoder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.klaytn.caver.base.Accounts.*;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static org.junit.Assert.assertEquals;

public class FeePayerManagerTest {

    static final BigInteger GAS_PRICE = Convert.toPeb("25", Convert.Unit.STON).toBigInteger();
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
    static final BigInteger FEE_RATIO = BigInteger.valueOf(30);

    private Caver caver;

    @Before
    public void setUp() {
        caver = Caver.build(Caver.DEFAULT_URL);
    }

    @Test
    public void testsetestset() throws Exception {
        KlayAccountKey response = caver.klay().getAccountKey("0xd6d6cc8037fbf0a28ce390395438caf91a1ac4a5", DefaultBlockParameterName.LATEST).send();

        KlayAccountKey.AccountKeyValue accountKey = response.getResult();
        TestCase.assertEquals(0x02, accountKey.getKeyType());
    }


    @Test
    public void accountGenerator() throws Exception {
        //KlayCredentials credentials = KlayCredentials.create(Keys.createEcKeyPair());
        //KlayCredentials credentials = KlayCredentials.create("0x7654c26091727c5f52401850c2f9e8d0a8b8daa1caee8c1c2bbab12a3891bb89");
        KlayCredentials credentials = KlayCredentials.create("51ca9e2c73208291b3158d0fd2362c81d202fcb24d0f5b395640e03029f35023");

        System.out.println(credentials.getAddress());
        System.out.println(credentials.getEcKeyPair().getPrivateKey().toString(16));

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = ValueTransfer.create(caver, BRANDON, LOCAL_CHAIN_ID).sendFunds(
                BRANDON.getAddress(),
                "0x58592f0c7799adb2c307257ae7403e43badc8b3e",
                BigDecimal.valueOf(1),
                Convert.Unit.KLAY, GAS_LIMIT
        ).send();

        assertEquals("0x1", transactionReceipt.getStatus());
    }

    @Test
    public void rolebasedAccountTest() throws Exception {
        KlayCredentials ROLESIG_TRANSACTION = KlayCredentials.create(ROLESIG.getEcKeyPairsForTransactionList(), null, null, ROLESIG.getAddress());
        KlayCredentials ROLESIG_TRANSACTION_0 = KlayCredentials.create(Arrays.asList(ROLESIG.getEcKeyPairsForTransactionList().get(0)), null, null, ROLESIG.getAddress());
        KlayCredentials ROLESIG_TRANSACTION_1 = KlayCredentials.create(Arrays.asList(ROLESIG.getEcKeyPairsForTransactionList().get(1)), null, null, ROLESIG.getAddress());
        KlayCredentials ROLESIG_TRANSACTION_2 = KlayCredentials.create(Arrays.asList(ROLESIG.getEcKeyPairsForTransactionList().get(2)), null, null, ROLESIG.getAddress());

        KlayCredentials ROLESIG_UPDATE = KlayCredentials.create(null, ROLESIG.getEcKeyPairsForUpdateList(), null, ROLESIG.getAddress());
        KlayCredentials ROLESIG_FEE = KlayCredentials.create(null, null, ROLESIG.getEcKeyPairsForFeePayerList(), ROLESIG.getAddress());

        KlayCredentials ROLESIG_MISS_TRANSACTION = KlayCredentials.create(ROLESIG.getEcKeyPairsForFeePayerList(), null, null, ROLESIG.getAddress());

        ValueTransferTransaction valueTransferTransaction = ValueTransferTransaction.create(ROLESIG.getAddress(), BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT).memo("test memo").feeDelegate();
        TransactionManager transactionManager = new TransactionManager.Builder(caver, ROLESIG_TRANSACTION_0)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 15))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        TransactionManager transactionManager_1 = new TransactionManager.Builder(caver, ROLESIG_TRANSACTION_1)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 15))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();


        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, ROLESIG_FEE).setChainId(LOCAL_CHAIN_ID).build();

        KlaySignatureData signatureData = transactionManager.makeSignatureData(valueTransferTransaction);
        KlayRawTransaction signedTransaction = transactionManager.sign(valueTransferTransaction);

        KlayRawTransaction feeSignedTransaction = feePayerManager.sign(signedTransaction.getValueAsString());
        //transactionManager_1.sign(feeSignedTransaction)


        transactionManager.executeTransaction(valueTransferTransaction);

        //assertEquals("0x1", transactionReceipt.getStatus());
        //System.out.println(transactionReceipt.getSignatures());


    }

    @Test
    public void testsets() throws Exception {
        KlayCredentials piction = KlayCredentials.create(
                "51ca9e2c73208291b3158d0fd2362c81d202fcb24d0f5b395640e03029f35023",
                "0xc73DB144887fEAE6150E01df9D48F0293f26256f"
        );

        KlayCredentials user = KlayCredentials.create(
                "7badbefbfce188e0d49e2f0b27bb6e9d0bde18e6f52238eecd40de0aa07c9eae",
                "0xd6d6cc8037fbf0a28ce390395438caf91a1ac4a5"
        );

        KlayCredentials user2 = KlayCredentials.create(
                new String[]{piction.getEcKeyPairsForTransactionList().get(0).getPrivateKey().toString(16)},
                new String[]{user.getEcKeyPairsForTransactionList().get(0).getPrivateKey().toString(16)},
                new String[]{},
                "0xd6d6cc8037fbf0a28ce390395438caf91a1ac4a5"
        );


        TransactionManager tansactionManager = new TransactionManager.Builder(caver, user2)
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, BRANDON)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                //.setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 100, 600))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        System.out.println(user.getEcKeyPair().getPublicKey().toString(16));

        AccountUpdateTransaction tx = AccountUpdateTransaction.create(
                user.getAddress(), // from
                AccountKeyRoleBased.create(Arrays.asList(
                        AccountKeyPublic.create(user.getEcKeyPair().getPublicKey()), // RoleTransaction
                        AccountKeyPublic.create(user.getEcKeyPair().getPublicKey()),
                        AccountKeyPublic.create(user.getEcKeyPair().getPublicKey()) // RoleTransaction
                        //AccountKeyPublic.create(piction.getEcKeyPair().getPublicKey()), // RoleAccountUpdate
                        //AccountKeyPublic.create(BRANDON.getEcKeyPair().getPublicKey()) // RoleAccountUpdate
                )), // accountKey
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction senderTx = tansactionManager.sign(tx, true);


        //println("tx: ${senderTx.valueAsString}")
        String senderString = senderTx.getValueAsString();
        KlayRawTransaction payerTx = feePayerManager.sign(senderString);
//        println("payer tx: ${payerTx.valueAsString}")
        feePayerManager.send(payerTx);

    }

    @Test
    public void kkk() throws Exception {

        StringBuffer sb = new StringBuffer(64);
        for (int i = 0; i < 4; i++) {
            sb.append("0");
        }

        String kkk = "33e3er";
        sb.append(kkk);


        System.out.println(sb.toString());
    }

    @Test
    public void testsets2() throws Exception {
        KlayCredentials piction = KlayCredentials.create(
                "51ca9e2c73208291b3158d0fd2362c81d202fcb24d0f5b395640e03029f35023",
                "0xc73DB144887fEAE6150E01df9D48F0293f26256f"
        );

        /*
        KlayCredentials user = KlayCredentials.create(
                "3723d07c082d2a010045fa2cb6d4b5563fadae15ddcc1cfc1a49700a6eafbb2",
                "0x41164aa682fce926103f1325e2f9e4d7f23cd345"
        );
*/
        KlayCredentials user = KlayCredentials.create(
                "68b61479ab4cabc1a149ea7285e11b7e61d33111756d4f97953a093ad6fba3ab",
                "0x58592f0c7799adb2c307257ae7403e43badc8b3e"
        );

        KlayCredentials user2 = KlayCredentials.create(
                new String[]{user.getEcKeyPairsForTransactionList().get(0).getPrivateKey().toString(16)},
                new String[]{piction.getEcKeyPairsForTransactionList().get(0).getPrivateKey().toString(16)},
                new String[]{},
                "0x41164aa682fce926103f1325e2f9e4d7f23cd345"
        );

        TransactionManager tansactionManager = new TransactionManager.Builder(caver, user)
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, BRANDON)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                //.setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 100, 600))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        System.out.println(user.getEcKeyPair().getPublicKey().toString(16));

        AccountUpdateTransaction tx = AccountUpdateTransaction.create(
                user.getAddress(), // from
                AccountKeyRoleBased.create(Arrays.asList(
                        AccountKeyPublic.create(user.getEcKeyPair().getPublicKey()) // RoleTransaction
                        //AccountKeyPublic.create(piction.getEcKeyPair().getPublicKey()), // RoleAccountUpdate
                        //AccountKeyPublic.create(BRANDON.getEcKeyPair().getPublicKey()) // RoleAccountUpdate
                )), // accountKey
                GAS_LIMIT // gasLimit
        ).feeDelegate();

        KlayRawTransaction senderTx = tansactionManager.sign(tx, true);


        //println("tx: ${senderTx.valueAsString}")
        String senderString = senderTx.getValueAsString();
        KlayRawTransaction payerTx = feePayerManager.sign(senderString);
//        println("payer tx: ${payerTx.valueAsString}")
        feePayerManager.send(payerTx);

    }

    @Test
    public void multisigCredentialTest() throws Exception {
        TransactionManager transactionManager = new TransactionManager.Builder(caver, MULTISIG)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 15))
                .setChaindId(LOCAL_CHAIN_ID)
                .build();

        ValueTransfer valueTransfer = ValueTransfer.create(caver, transactionManager);

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = valueTransfer.sendFunds(MULTISIG.getAddress(), BRANDON.getAddress(), BigDecimal.valueOf(1), Convert.Unit.PEB, GAS_LIMIT).send();
        assertEquals("0x1", transactionReceipt.getStatus());
        //transactionManager.executeTransaction();

    }

    @Test
    public void decodeTest() {
        //0x20f901a8808505d21dba0083419ce094da1b6872d83e0c0d6f1dadeafc724d15f24dbff7b9013c05f90138b87204f86f02f86ce301a1039de7ec07d96083d5df216f7dfaefbab795e6e8fba8cee9b3b5d6cd45960c1212e301a102a720390669f70f4037b2fbf605c0249132c564277c3365860e91f1b6091d9340e301a103ab2f2478a390bb17897bb7f5e6ecaba4312dc8bb7482225cda586551c8ad9127b84e04f84b02f848e301a102d14ae8ba92ddd6eb263ff2ff4c8f777750e9f19d1e981cab89f686ffb5f063cae301a10338f98936ef860d5b6d9c5505dc053acb0242a7809e322ccbff193280c9e6552bb87204f86f02f86ce301a102d405aacfb1fbd3c91703856cd8b1829750179c9c2acf8167d8d6708d49e11400e301a1037c98fef2fb1c7424e49c62dc0ab12ab7e8c253e4ba70a5e3d777389b2813f130e301a102804e602553ecd0776bae0a3ed3a982621e4eebe6626e70fd062c0db65717e15df847f845820fe9a028e1a4f72b4b3e875ad9fa2127935cf671a63797848fdf7d9ed311e330d0cdd6a04f9bd532e4a2a1ae177d1898a0d6603e0ebb26ef626e58084720658b72e34ad6
        AbstractTxType rolebasedTx = TransactionDecoder.decode("0x20f901a8808505d21dba0083419ce094da1b6872d83e0c0d6f1dadeafc724d15f24dbff7b9013c05f90138b87204f86f02f86ce301a1039de7ec07d96083d5df216f7dfaefbab795e6e8fba8cee9b3b5d6cd45960c1212e301a102a720390669f70f4037b2fbf605c0249132c564277c3365860e91f1b6091d9340e301a103ab2f2478a390bb17897bb7f5e6ecaba4312dc8bb7482225cda586551c8ad9127b84e04f84b02f848e301a102d14ae8ba92ddd6eb263ff2ff4c8f777750e9f19d1e981cab89f686ffb5f063cae301a10338f98936ef860d5b6d9c5505dc053acb0242a7809e322ccbff193280c9e6552bb87204f86f02f86ce301a102d405aacfb1fbd3c91703856cd8b1829750179c9c2acf8167d8d6708d49e11400e301a1037c98fef2fb1c7424e49c62dc0ab12ab7e8c253e4ba70a5e3d777389b2813f130e301a102804e602553ecd0776bae0a3ed3a982621e4eebe6626e70fd062c0db65717e15df847f845820fe9a028e1a4f72b4b3e875ad9fa2127935cf671a63797848fdf7d9ed311e330d0cdd6a04f9bd532e4a2a1ae177d1898a0d6603e0ebb26ef626e58084720658b72e34ad6");

        //AbstractTxType tx = TransactionDecoder.decode("0x08f90271028505d21dba0083419ce094e97f27e9a5765ce36a7b919b1cb6004c7209217e019403899059faf55f512ebccfe6a46327973e7d6dd3f90238f845820fe9a0d2d17cbb14aa153cf97f4b931d1ba3d3897ca346d1ad4a59c57a89d9ec70c377a026876185251664f7fc07e75c20652ca34a1f5e649422a4d787ac1c345552e21ff845820feaa09da289ba8e063cbc20afdbd84bf3b8222892568062503abb852f23440c64e2f0a0769799ee636ea50d25829a91510f12be57091ebcc4af5398e4062b10685547fff845820fe9a0ab3a11314be5d2b69ea907fc1d6b825d0d61677ebdb93ca6d3b15ade047a9b00a04247835b49fb71878b4a6e7bccb155d61a8a3931a120497df47d66f00dc0de3af845820feaa0b8bbc4d53061db9fea92134e9d3c7f81d291226fd4dd15c15ee485f8117b8a99a072343bd95ba2483c24db9e96cb028e686b5a1d5417ed4b425a73b0f63626dc38f845820feaa083ff5e2695f21c2cd91fc288a37f3872433864428328ebb3dda032c6be1b6775a02a853de9c8061200bdcd135736c998d5979900d68ae038e7284e32a9a979a403f845820fe9a005034d0b86aab635f50958414372d3c9d91d7f361727dee2db6d45e33601b7e1a009e8286540666c3b9e9d7b3418feb22406e4fc0bd087c55b1fe6fcbf5a22d627f845820feaa0195782ed80696de6e82155929e6970c00a2e05d38b8c935347de1aabe30481e3a022e8abc8f3bd5c541c477f688e2ab1adcc57da8259109381681dc24070608098f845820fe9a034dceb0d864c733be5d58190e972ec0b358991c869027a06f1f6f8fd68b6d855a04bba51ffb234dac23888a015e3567489b4fecdd3fbf9e58eb30a84ec54a4b0c7");
        AbstractTxType tx = TransactionDecoder.decode("0x21f90114808505d21dba0083419ce094d6d6cc8037fbf0a28ce390395438caf91a1ac4a5b84b05f848a302a10205faac13128c12e04f5c4999225cbbedca8d066643b89479e21c9ddf2546d892a302a10263eb880cf922b6bdb4115e1d72560438051b9c24d4cd6e3b8b8ce88fd506ae60f847f845820fe9a02c99d2de0c4a118ac3962c5eb03b4e3cc96b2712c258f426311eb722611094e9a03464b3bf39da620ceaae256e7edef0404160bafbfa6ae4325efac95649aab00194e97f27e9a5765ce36a7b919b1cb6004c7209217ef847f8458207f6a0d3d64f6e770261992dab03211902c2196ef45419ee4c252105a0708358914c79a0359cd498d5f3d92c97897f97843fdd28fb022bb390387271633f1ccf93f79c92");


        System.out.println(tx);
    }


    @Test
    public void myTest4() throws Exception {
        List<ECKeyPair> a = new ArrayList<ECKeyPair>();
        List<ECKeyPair> b = new ArrayList<ECKeyPair>();

        ECKeyPair k = ECKeyPair.create(Numeric.toBigInt("1234"));
        ECKeyPair l = ECKeyPair.create(Numeric.toBigInt("1234"));

        ECKeyPair m = ECKeyPair.create(Numeric.toBigInt("12345"));
        ECKeyPair n = ECKeyPair.create(Numeric.toBigInt("12345"));

        a.add(k);
        a.add(n);
        System.out.println(a.hashCode());
        b.add(l);
        b.add(n);
        System.out.println(b.hashCode());


    }

    public List<String> getList() {
        return null;
    }


    @Test
    public void testFeePayerManagerValueTransferJasmine() throws Exception {
        String rawTx = getSenderRawTx();
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, FEE_PAYER)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(rawTx);

        assertEquals("0x1", transactionReceipt.getStatus());
        assertEquals(FEE_PAYER.getAddress(), transactionReceipt.getFeePayer());
        assertEquals(Numeric.toHexStringWithPrefixSafe(FEE_RATIO), transactionReceipt.getFeeRatio());
        assertEquals(feePayerManager.sign(rawTx).getSignatureData(), transactionReceipt.getFeePayerSignatures().get(0));
    }

    @Test
    public void testFeePayerManagerValueTransfer() throws Exception {
        String rawTx = getSenderRawTx();
        FeePayerManager feePayerManager = new FeePayerManager.Builder(caver, FEE_PAYER)
                .setTransactionReceiptProcessor(new PollingTransactionReceiptProcessor(caver, 1000, 10))
                .setChainId(LOCAL_CHAIN_ID)
                .build();

        KlayTransactionReceipt.TransactionReceipt transactionReceipt = feePayerManager.executeTransaction(rawTx);

        assertEquals("0x1", transactionReceipt.getStatus());
        assertEquals(FEE_PAYER.getAddress(), transactionReceipt.getFeePayer());
        assertEquals(Numeric.toHexStringWithPrefixSafe(FEE_RATIO), transactionReceipt.getFeeRatio());
        assertEquals(feePayerManager.sign(rawTx).getSignatureData(), transactionReceipt.getFeePayerSignatures().get(0));
    }

    private String getSenderRawTx() throws Exception {
        TxType tx = ValueTransferTransaction.create(LUMAN.getAddress(), BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT)
                .gasPrice(GAS_PRICE)
                .nonce(getNonce(LUMAN.getAddress()))
                .feeRatio(FEE_RATIO)
                .buildFeeDelegated();
        return tx.sign(LUMAN, LOCAL_CHAIN_ID).getValueAsString();
    }

    private String getDoubleSenderRawTx() throws Exception {
        TxType tx = ValueTransferTransaction.create(JASMINE1.getAddress(), BRANDON.getAddress(), BigInteger.ONE, GAS_LIMIT)
                .gasPrice(GAS_PRICE)
                .nonce(getNonce(JASMINE1.getAddress()))
                .feeRatio(FEE_RATIO)
                .buildFeeDelegated();

        String singnedTx = tx.sign(JASMINE1, LOCAL_CHAIN_ID).getValueAsString();
        TxType tx2 = FeePayerTransactionDecoder.decode(singnedTx);
        String singnedTx2 = tx2.sign(JASMINE2, LOCAL_CHAIN_ID).getValueAsString();
        TxType tx3 = FeePayerTransactionDecoder.decode(singnedTx2);
        String singnedTx3 = tx3.sign(JASMINE3, LOCAL_CHAIN_ID).getValueAsString();

        return singnedTx3;
    }

    BigInteger getNonce(String address) throws Exception {
        BigInteger nonce = caver.klay().getTransactionCount(
                address,
                DefaultBlockParameterName.PENDING).sendAsync().get().getValue();
        return nonce;
    }
}

