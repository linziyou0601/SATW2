package com.satw.demo.Blockchain;

import java.util.LinkedList;

public class Deposit extends Transaction {
    private String receiverAddress;
    //-------------------- 建構子、Getter、Setter --------------------
    public Deposit(String publicKey, String receiverAddress, int amount){
        super(publicKey, "Deposit "+Integer.toString(amount), amount, null, "Deposit");
        this.receiverAddress = receiverAddress;
    }
    public String getReceiverAddress(){ return receiverAddress; }

    //交易處理
    private void deposit(){
        LinkedList<TransactionOutput> outputs = super.getOutputs();
        //建立交易輸出金流
        outputs.add(new TransactionOutput(receiverAddress, super.getAmount(), super.getHash()));
        //將新的UTXO放到鏈的UTXO清單中
        for(TransactionOutput output: super.getOutputs()) Blockchain.putUTXOs(output.getHash(), output);
    }

    //-------------------- concrete method --------------------
    public boolean processTransaction() {
        //驗證交易簽名
        if(super.getSignature()!=null && verifiySignature() == false) {
            System.out.println("[x] 交易簽名驗證失敗"); //prompt
            return false;
        }
        //驗證交易Hash值
        if(super.getHash() == calculateHash()) {
            System.out.println("[x] 交易Hash值有誤"); //prompt
            return false;
        }
        deposit();
        return true;
    }

    public String hashPlainData() {
        return (receiverAddress +
                super.getDetail() +
                Integer.toString(super.getAmount()) +
                Integer.toString(super.getSequence())
        );
    }

    public boolean verifyOwner(String address){
        return this.receiverAddress.equals(address);
    }
}
