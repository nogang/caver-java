package com.klaytn.caver.study;

import com.klaytn.caver.crypto.KlayCredentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

public class Study {
    public static void main(String[] args){
        System.out.println("test");
        //test();
        A a = new A("before");
        a.Print();

        test2(a);
        a.Print();
    }

    public static void test2(A a) {
        a = new A("test2");
        a.Print();
    }

    public static void test() {
        try {
            ECKeyPair[] updateKeys = {Keys.createEcKeyPair(),Keys.createEcKeyPair()};
            KlayCredentials.create(null, updateKeys,null,"0x2c8ad0ea2e0781db8b8c9242e07de3a5beabb71a");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

class A {
    private String test;
    public A(String test) {
        this.test = test;
    }
    public void Print(){
        System.out.println(test);
    }
}

/*
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
*/