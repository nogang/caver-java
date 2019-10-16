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

package com.klaytn.caver.base;

import com.klaytn.caver.Caver;
import com.klaytn.caver.crypto.KlayCredentials;
import com.klaytn.caver.tx.manager.TransactionManager;
import com.klaytn.caver.tx.model.ValueTransferTransaction;
import com.klaytn.caver.utils.Convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.klaytn.caver.base.LocalValues.KLAY_PROVIDER;
import static com.klaytn.caver.base.LocalValues.LOCAL_CHAIN_ID;

public class Accounts {

    public static final KlayCredentials LUMAN = KlayCredentials.create(
            "0x2359d1ae7317c01532a58b01452476b796a3ac713336e97d8d3c9651cc0aecc3",
            "0x2c8ad0ea2e0781db8b8c9242e07de3a5beabb71a"
    );

    public static final KlayCredentials WAYNE = KlayCredentials.create(
            "0x92c0815f28b20cc22fff5fcf41adc80efe9d7ebe00439628b468f2f88a0aadc4",
            "0x3cd93ba290712e6d28ac98f2b820faf799ae8fdb"
    );

    public static final KlayCredentials BRANDON = KlayCredentials.create(
            "0x734aa75ef35fd4420eea2965900e90040b8b9f9f7484219b1a06d06394330f4e",
            "0xe97f27e9a5765ce36a7b919b1cb6004c7209217e"
    );

    public static final KlayCredentials FEE_PAYER = KlayCredentials.create(
            "0x1e558ea00698990d875cb69d3c8f9a234fe8eab5c6bd898488d851669289e178",
            "0x9d0dcbe163be73163348e7f96accb2b9e1e9dcf6"
    );

    public static final KlayCredentials JASMINE1 = KlayCredentials.create(
            "0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8",
            "0xf63ac5a84fd520eeb4288bd1400c6564cd034391"
    );

    public static final KlayCredentials JASMINE1_UP = KlayCredentials.create(
            "0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8",
            "0xe97f27e9a5765ce36a7b919b1cb6004c7209217e"
    );

    public static final KlayCredentials JASMINE2 = KlayCredentials.create(
            "0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422",
            "0xf63ac5a84fd520eeb4288bd1400c6564cd034391"
    );

    public static final KlayCredentials JASMINE3 = KlayCredentials.create(
            "0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081",
            "0xf63ac5a84fd520eeb4288bd1400c6564cd034391"
    );

    //0x03899059faf55f512ebccfe6a46327973e7d6dd3 address
    //47089913043240590419316349747693989396475169353732431002650095452839095744856 private key
    public static final KlayCredentials MULTISIG = KlayCredentials.create(
            new String[]{"0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8",
                    "0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422",
                    "0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3",
                    "0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081",
                    "0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad",
                    "0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823",
                    "0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196",
                    "0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120"},
            "0x03899059faf55f512ebccfe6a46327973e7d6dd3"
    );
    public static final int[] MULTISIG_WEIGHT = new int[]{1, 2, 3,2,1,2,3,2};


    public static final KlayCredentials ROLESIG = KlayCredentials.create(

            new String[]{"0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8",
                    "0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422",
                    "0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3"
            },
            new String[]{"0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081",
                    "0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad"},
            new String[]{"0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823",
                    "0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196",
                    "0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120"},
            "0x7504fec6880e490d47e88356fd75f59c86fcb090"
            );
    /*
    0x7504fec6880e490d47e88356fd75f59c86fcb090
7654c26091727c5f52401850c2f9e8d0a8b8daa1caee8c1c2bbab12a3891bb89
    EN EP ===> ‘http://52.79.239.136:8551/’
<MultiSigAccount>
1. address : 0xf63ac5a84fd520eeb4288bd1400c6564cd034391
   original privateKey : 0xbf3d88e25880ebd3008c1166263b34368dfe8c9c4d8bb9ad8dd9357f1b2632b0
   threshold : 3
   weight:1, ‘0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8’
   weight:2, ‘0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422’
   weight:3, ‘0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3’
   weight:2, ‘0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081’
   weight:1, ‘0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad’
   weight:2, ‘0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823’
   weight:3, ‘0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196’
   weight:2, ‘0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120’
2. address : 0x0b16208cc56255d47424bF976B0F85FB8174E4A5
   original privateKey : 0x68e0c107d571e6de36ac01e72f5f65b868cb066deceb6d0f593e23f3dbda9062
   threshold : 3
   weight:1, ‘0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8’
   weight:2, ‘0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422’
   weight:3, ‘0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3’
   weight:2, ‘0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081’
   weight:1, ‘0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad’
   weight:2, ‘0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823’
   weight:3, ‘0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196’
   weight:2, ‘0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120’ (edited)
     */
/*
EN IP ===> ‘http://13.124.143.27:8551’
multisig account address ===>
multisig account original privateKey(before update) ===> 0xbf3d88e25880ebd3008c1166263b34368dfe8c9c4d8bb9ad8dd9357f1b2632b0
multisig account threshold ===> 3
multisig account private keys ===>
weight:1, ‘0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8’
weight:2, ‘0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422’
weight:3, ‘0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3’
weight:2, ‘0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081’
weight:1, ‘0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad’
weight:2, ‘0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823’
weight:3, ‘0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196’
weight:2, ‘0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120’ (edited)
 */

/*
<RoleBasedAccount>
1. address : 0xF1fE84042FA7f7E63Abd2a0fc793C786C546f56b
  original privateKey : 0x6748a0b54b57adad861a02ada0eba0a637db36ade699fae93b0f717d934ca7a5
  transactionKey ===>
       threshold : 2
       weight:1, ‘0x86bad9d7f0b8c7498aa9155f629f816a961ed0ee8e4c47cbf71158402caafaa8’
       weight:1, ‘0xda653362fa525d2d55e2f822fad8ebc9adbbcb326aeb546ae168ae01f6e99422’
       weight:1, ‘0xd41b273e49df72316442110437a7c2970a752b63de8204898f293265eae79de3’
  updateKey ===>
       threshold : 2
       weight:1, ‘0x4a37a651de064ed0fa7794805d6f9317cac34c6f3ae4f04afe0b4de22ba7a081’
       weight:1, ‘0x9b25fd4c3442f97be6e7a06f9eb701bbd7bdc00327b84e8b76755086723747ad’
  feePayerKey ===>
       threshold : 2
       weight:1, ‘0xe62bfc702379370405ef1f1ff49e71730cfdd989fa3c5313b6544179a2f52823’
       weight:1, ‘0x7dfb0135888458c4c7eed67360b0369ef6e514504fb33e79d3f7675bda0e6196’
       weight:1, ‘0xf2df7c1c422fb64d7838836ff11d97ec5b941136a03511202c9e18ab7aec0120’
2. address : 0xb829D2474E6929686972BA1A56aDe8dEf42c0fFf
 */
    static {
        List<KlayCredentials> testCredentials = new ArrayList<>(Arrays.asList(LUMAN, WAYNE, BRANDON, FEE_PAYER));
        fillUpKlay(testCredentials);
    }

    private static void fillUpKlay(List<KlayCredentials> testCredentials) {
        Caver caver = Caver.build(Caver.DEFAULT_URL);
        TransactionManager transactionManager
                = new TransactionManager.Builder(caver, KLAY_PROVIDER).setChaindId(LOCAL_CHAIN_ID).build();
        for (KlayCredentials testCredential : testCredentials) {
            feedToEach(transactionManager, testCredential);
        }
    }

    private static void feedToEach(TransactionManager transactionManager, KlayCredentials testCredential) {
        ValueTransferTransaction valueTransferTransaction = ValueTransferTransaction.create(
                KLAY_PROVIDER.getAddress(),
                testCredential.getAddress(),
                Convert.toPeb("100", Convert.Unit.KLAY).toBigInteger(),
                BigInteger.valueOf(4_300_000)
        );
        transactionManager.executeTransaction(valueTransferTransaction);
    }
}
