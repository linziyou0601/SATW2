package com.satw.demo.Blockchain;

import java.time.Instant;
import java.util.LinkedList;

import com.satw.demo.Util.KeyPairUtil;
import com.satw.demo.Util.StringUtil;

public abstract class Transaction {
    public static int nonce;       //prevent same sha256
    private String hash;           //SHA256(timestamp + payer + receiver + detail + amount + nonce)
    private String signature;
    private String publicKey;
    private String detail;
    private int amount;
    private long timestamp;
    private int sequence;
    private LinkedList<TransactionInput> inputs = new LinkedList<>();
    private LinkedList<TransactionOutput> outputs = new LinkedList<>();
    //ORM
    private String classType;

    Transaction(String publicKey, String detail, int amount, LinkedList<TransactionInput> inputs, String classType){
        this.timestamp = Instant.now().toEpochMilli();
        this.publicKey = publicKey;
        this.detail = detail;
        this.amount = amount;
        this.inputs = inputs;
        this.sequence = nonce++;
        this.hash = this.calculateHash();
        this.classType = classType;
    }
    // getter
    public String getHash(){ return hash; }
    public String getDetail(){ return detail; }
    public int getAmount(){ return amount; }
    public String getSignature(){ return signature; }
    public long getTimestamp(){ return timestamp; }
    public int getSequence(){ return sequence; }
    public LinkedList<TransactionInput> getInputs(){ return inputs; }
    public LinkedList<TransactionOutput> getOutputs(){ return outputs; }
    public String getClassType(){ return classType; }

    // operator
    //-------------------- transaction process --------------------
    public int getInputsAmount() {
        int total = 0;
        for(TransactionInput input: inputs)
            total += input.getAmount();
        return total;
    }
    public int getOutputsAmount() {
        int total = 0;
        for(TransactionOutput output: outputs)
            total += output.getAmount();
        return total;
    }
    public abstract boolean processTransaction();
    public abstract String hashPlainData();
    public String calculateHash(){
        return StringUtil.SHA256(this.hashPlainData());
    }

    //-------------------- encrypt and signature --------------------
    //encrypt using private key
    public void generateSignature(String privateKey) {
        String plainData = hashPlainData();
        signature = StringUtil.bytesToString(
                KeyPairUtil.applyECDSASig(
                        KeyPairUtil.stringToPrivateKey(privateKey),
                        plainData
                )
        );
    }
    //verify using public key
    public boolean verifiySignature() {
        String plainData = hashPlainData();
        return KeyPairUtil.verifyECDSASig(
                KeyPairUtil.stringToPublicKey(publicKey),
                plainData,
                StringUtil.stringToBytes(signature)
        );
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transaction other = (Transaction) obj;
        if (!this.hash.equals(other.getHash()))
            return false;
        return true;
    }
}