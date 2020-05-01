package com.satw.demo.Blockchain;

import java.util.LinkedList;

public class Deposit extends Transaction {
    private int receiverId;
    //-------------------- 建構子、Getter、Setter --------------------
    public Deposit(String publicKey, int receiverId, int amount){
        super(publicKey, "Deopsit "+Integer.toString(amount), amount, null, "Deposit");
        this.receiverId = receiverId;
    }
    public int getReceiverId(){ return receiverId; }

    //交易處理
    private void deposit(){
        LinkedList<TransactionOutput> outputs = super.getOutputs();
        //建立交易輸出金流
        outputs.add(new TransactionOutput(receiverId, super.getAmount(), super.getHash()));
        //將新的UTXO放到鏈的UTXO清單中
        for(TransactionOutput output: super.getOutputs()) Blockchain.putUTXOs(output.getHash(), output);
    }

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
        return (Integer.toString(receiverId) +
                super.getDetail() +
                Integer.toString(super.getAmount()) +
                Integer.toString(super.getSequence())
        );
    }
}
