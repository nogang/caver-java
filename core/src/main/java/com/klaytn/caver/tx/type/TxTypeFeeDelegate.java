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

import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.crypto.KlaySignatureData;
import com.klaytn.caver.tx.exception.EmptyNonceException;
import com.klaytn.caver.tx.model.KlayRawTransaction;
import com.klaytn.caver.utils.BytesUtils;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This interface represents which is fee delegated transaction type
 */
public abstract class TxTypeFeeDelegate extends AbstractTxType{
    private Set<KlaySignatureData> feePayerSignatureDataSet;
    private String feePayer;
    final static String EMPTY_FEE_PAYER_ADDRESS = "0x30";
    public TxTypeFeeDelegate(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit,
                          String from, String to, BigInteger value) {
        super(nonce,gasPrice,gasLimit,from,to,value);
        this.feePayerSignatureDataSet = new HashSet<>();
        this.feePayer = EMPTY_FEE_PAYER_ADDRESS;
    }

    public void addFeePayerSignatureData(KlaySignatureData signatureData){
        feePayerSignatureDataSet.add(signatureData);
    }

    public void addFeePayerSignatureDataList(Set<KlaySignatureData> feePayerSignatureDataSet){
        this.feePayerSignatureDataSet.addAll(feePayerSignatureDataSet);
    }

    protected void addFeePayerSignatureData(List<RlpType> signatureRlpTypeList) {
        for (RlpType signature : signatureRlpTypeList) {
            List<RlpType> vrs = ((RlpList) signature).getValues();
            if (vrs.size() < 3) continue;
            byte[] v = ((RlpString) vrs.get(0)).getBytes();
            byte[] r = ((RlpString) vrs.get(1)).getBytes();
            byte[] s = ((RlpString) vrs.get(2)).getBytes();
            addFeePayerSignatureData(new KlaySignatureData(v, r, s));
        }
    }
    public Set<KlaySignatureData> getFeePayerSignatureDataSet() {
        return feePayerSignatureDataSet;
    }

    public void setFeePayer(String feePayer){
        this.feePayer = feePayer;
    }

    public String getFeePayer(){
        return this.feePayer;
    }

    public void addSignatureData(List<RlpType> values, int offset) {
        if (values.size() > offset) {
            List<RlpType> senderSignatures = ((RlpList) (values.get(offset))).getValues();
            addSenderSignatureData(senderSignatures);
        }

        if (values.size() > offset+1) {
            String feePayer = ((RlpString) values.get(offset+1)).asString();
            setFeePayer(feePayer);
        }

        if (values.size() > offset+2) {
            List<RlpType> feePayerSignatures = ((RlpList) (values.get(offset+2))).getValues();
            addFeePayerSignatureData(feePayerSignatures);
        }
    }

    public void addSignatureData(TxTypeFeeDelegate txType) {
        addSenderSignatureData(txType.getSenderSignatureDataSet());
        addFeePayerSignatureDataList(txType.getSenderSignatureDataSet());
    }

    public KlayRawTransaction sign(KlayCredentials credentials, int chainId) {
        // it's for not fee delegate, it's sign for sender
        if (nonce == null) {
            throw new EmptyNonceException();
        }
        Set<KlaySignatureData> signatureDataSet = getSenderSignatureDataSet(credentials, chainId);
        List<RlpType> rlpTypeList = new ArrayList<>(rlpValues());

        List<RlpType> senderSignatureList = new ArrayList<>();

        this.senderSignatureDataSet.addAll(signatureDataSet);

        for (KlaySignatureData klaySignatureData : this.senderSignatureDataSet) {
            senderSignatureList.add(klaySignatureData.toRlpList());
        }

        rlpTypeList.add(new RlpList(senderSignatureList));

        // todo: it should be able to sign regardless of the order of feepayer and sender.
        rlpTypeList.add(RlpString.create(Numeric.hexStringToByteArray(this.feePayer)));

        if (this.feePayer.equals("0x30")){
            rlpTypeList.add(new RlpList(KlaySignatureData.createKlaySignatureDataFromChainId(1).toRlpList()));
        } else {
            List<RlpType> feePayerSignatureList = new ArrayList<>();
            for (KlaySignatureData klaySignatureData : this.feePayerSignatureDataSet) {
                feePayerSignatureList.add(klaySignatureData.toRlpList());
            }
            rlpTypeList.add(new RlpList(feePayerSignatureList));
        }

        byte[] encodedTransaction = RlpEncoder.encode(new RlpList(rlpTypeList));
        byte[] type = {getType().get()};
        byte[] rawTx = BytesUtils.concat(type, encodedTransaction);

        StringBuilder sb = new StringBuilder();
        for(final byte b: rawTx)
            sb.append(String.format("%02x ", b&0xff));
        System.out.println(sb.toString());

        return new KlayRawTransaction(rawTx, signatureDataSet); //todo: check why it need signatuedata
    }

    public BigInteger getFeeRatio() {
        return BigInteger.valueOf(100);
    };
}
