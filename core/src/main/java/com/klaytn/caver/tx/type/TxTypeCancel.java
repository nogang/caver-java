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

package com.klaytn.caver.tx.type;

import com.klaytn.caver.utils.KlayTransactionUtils;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * TxTypeCancel cancels the transaction with the same nonce in the txpool.
 */
public class TxTypeCancel extends AbstractTxType {
    protected TxTypeCancel(
            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String from) {
        super(nonce, gasPrice, gasLimit, from, "", BigInteger.ZERO);
    }

    public static TxTypeCancel createTransaction(
            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String from) {
        return new TxTypeCancel(nonce, gasPrice, gasLimit, from);
    }

    /**
     * create RlpType List which contains nonce, gas price, gas limit and from.
     * List elements can be different depending on transaction type.
     *
     * @return List RlpType List
     */
    @Override
    public List<RlpType> rlpValues() {
        List<RlpType> values = super.rlpValues();
        values.add(RlpString.create(Numeric.hexStringToByteArray(getFrom())));
        return values;
    }

    /**
     * This method is overridden as CANCEL type.
     * The return value is used for rlp encoding.
     *
     * @return Type transaction type
     */
    @Override
    public TxType.Type getType() {
        return Type.CANCEL;
    }

    public static TxTypeCancel decodeFromRawTransaction(byte[] rawTransaction) {
        // SenderTxHashRLP = type + encode([nonce, gasPrice, gas, from, txSignatures])
        try {
            byte[] rawTransactionExceptType = KlayTransactionUtils.getRawTransactionNoType(rawTransaction);

            RlpList rlpList = RlpDecoder.decode(rawTransactionExceptType);
            List<RlpType> values = ((RlpList) rlpList.getValues().get(0)).getValues();
            BigInteger nonce = ((RlpString) values.get(0)).asPositiveBigInteger();
            BigInteger gasPrice = ((RlpString) values.get(1)).asPositiveBigInteger();
            BigInteger gasLimit = ((RlpString) values.get(2)).asPositiveBigInteger();
            String from = ((RlpString) values.get(3)).asString();

            TxTypeCancel tx
                    = TxTypeCancel.createTransaction(nonce, gasPrice, gasLimit, from);

            tx.addSignatureData(values, 4);

            return tx;
        } catch (Exception e) {
            throw new RuntimeException("Incorrectly encoded tx.");
        }
    }
    /**
     * @param rawTransaction RLP-encoded signed transaction from sender
     * @return TxTypeCancel decoded transaction
     */
    public static TxTypeCancel decodeFromRawTransaction(String rawTransaction) {
        return decodeFromRawTransaction(Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(rawTransaction)));
    }

}
