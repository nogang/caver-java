/*
 * Modifications copyright 2019 The caver-java Authors
 * Copyright 2016 Conor Svensson
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
 *
 * This file is derived from web3j/integration-tests/src/test/java/org/web3j/protocol/scenarios/Scenario.java (2019/06/13).
 * Modified and improved for the caver-java development.
 */

package com.klaytn.caver.scenario;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.fee.FeePayerManager;
import com.klaytn.caver.methods.response.Bytes32;
import com.klaytn.caver.methods.response.KlayTransactionReceipt;
import com.klaytn.caver.tx.ValueTransfer;
import com.klaytn.caver.tx.account.AccountKeyPublic;
import com.klaytn.caver.tx.account.AccountKeyWeightedMultiSig;
import com.klaytn.caver.tx.type.TxType;
import com.klaytn.caver.utils.Convert;
import org.junit.Before;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.klaytn.caver.base.Accounts.BRANDON;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * Common methods & settings used across scenarios
 */
public class Scenario {

    static final BigInteger GAS_PRICE = Convert.toPeb("25", Convert.Unit.STON).toBigInteger();
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);
    static final StaticGasProvider STATIC_GAS_PROVIDER = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

    private static final String WALLET_PASSWORD = "";

    private static final BigInteger ACCOUNT_UNLOCK_DURATION = BigInteger.valueOf(30);

    private static final int SLEEP_DURATION = 2000;
    private static final int ATTEMPTS = 20;

    Caver caver;

    public Scenario() {
    }

    @Before
    public void setUp() {
        this.caver = Caver.build(Caver.DEFAULT_URL);
    }

    BigInteger getNonce(String address) throws Exception {
        return caver.klay().getTransactionCount(
                address,
                DefaultBlockParameterName.PENDING).sendAsync().get().getValue();
    }

    String signTransaction(TxType tx, KlayCredentials credentials) {
        return tx.sign(credentials, LOCAL_CHAIN_ID).getValueAsString();
    }

    String signTransactionFromFeePayer(String senderRawTx, KlayCredentials feePayer) {
        FeePayerManager feePayerManager = new FeePayerManager
                .Builder(this.caver, feePayer).setChainId(LOCAL_CHAIN_ID).build();
        return feePayerManager.sign(senderRawTx).getValueAsString();
    }

    KlayTransactionReceipt.TransactionReceipt sendTxAndGetReceipt(String rawTx) throws Exception {
        Bytes32 response = caver.klay().sendSignedTransaction(rawTx).send();
        return waitForTransactionReceipt(response.getResult());
    }

    KlayTransactionReceipt.TransactionReceipt waitForTransactionReceipt(
            String transactionHash) throws Exception {
        Optional<KlayTransactionReceipt.TransactionReceipt> transactionReceiptOptional =
                getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);

        if (!transactionReceiptOptional.isPresent()) {
            fail("Transaction receipt not generated after " + ATTEMPTS + " attempts");
        }
        return transactionReceiptOptional.get();
    }

    private Optional<KlayTransactionReceipt.TransactionReceipt> getTransactionReceipt(
            String transactionHash, int sleepDuration, int attempts) throws Exception {
        Optional<KlayTransactionReceipt.TransactionReceipt> receiptOptional =
                sendTransactionReceiptRequest(transactionHash);
        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }
        return receiptOptional;
    }

    private Optional<KlayTransactionReceipt.TransactionReceipt> sendTransactionReceiptRequest(
            String transactionHash) throws Exception {
        Response<KlayTransactionReceipt.TransactionReceipt> transactionReceipt =
                caver.klay().getTransactionReceipt(transactionHash).sendAsync().get();
        return Optional.ofNullable(transactionReceipt.getResult());
    }

    protected List<ECKeyPair> createECKeyPairList(int size) throws Exception{
        List<ECKeyPair> ecKeyPairList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ecKeyPairList.add(Keys.createEcKeyPair());
        }

        return ecKeyPairList;
    }

    protected void chargeAccount(String address) throws Exception{
        KlayTransactionReceipt.TransactionReceipt transactionReceipt = ValueTransfer.create(caver, BRANDON, LOCAL_CHAIN_ID).sendFunds(
                BRANDON.getAddress(),
                address,
                BigDecimal.valueOf(1),
                Convert.Unit.KLAY, GAS_LIMIT
        ).send();

        assertEquals("0x1", transactionReceipt.getStatus());
    }

    protected KlayCredentials createAccount() throws Exception{
        KlayCredentials user = KlayCredentials.create(Keys.createEcKeyPair());
        chargeAccount(user.getAddress());
        return user;
    }

    protected KlayCredentials createAccount(KlayCredentials oldCredentials) throws Exception{
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        KlayCredentials user = KlayCredentials.create(ecKeyPair.getPrivateKey().toString(16),oldCredentials.getAddress());
        chargeAccount(user.getAddress());
        return user;
    }

    protected AccountKeyWeightedMultiSig createRandomAccountKeyWeightedMultiSig(List<ECKeyPair> ecKeyPairList) throws Exception{
        Random random = new Random();
        List<AccountKeyWeightedMultiSig.WeightedPublicKey> weightedTransactionPublicKeys = new ArrayList<>();
        int sumOfWeight = 0;
        for (ECKeyPair ecKeyPair : ecKeyPairList) {
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
