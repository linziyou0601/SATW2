package com.satw.demo.Blockchain;

import java.util.LinkedList;

public class Payment extends Transaction {
    private int payerId;
    private int receiverId;
    private int orderId;
    //-------------------- 建構子、Getter、Setter --------------------
    public Payment(String publicKey, int payerId, int receiverId, int orderId, String detail, int amount, LinkedList<TransactionInput> inputs){
        super(publicKey, detail, amount, inputs, "Payment");
        this.payerId = payerId;
        this.receiverId = receiverId;
        this.orderId = orderId;
    }
    public int getPayerId(){ return payerId; }
    public int getReceiverId(){ return receiverId; }
    public int getOrderId(){ return orderId; }

    //交易處理
    private void payment(){
        LinkedList<TransactionOutput> outputs = super.getOutputs();
        //建立交易輸出金流
        int restAmount = getInputsAmount() - super.getAmount();
        outputs.add(new TransactionOutput(receiverId, super.getAmount(), super.getHash()));
        if(restAmount>0) outputs.add(new TransactionOutput(payerId, restAmount, super.getHash()));
        //將新的UTXO放到鏈的UTXO清單中
        for(TransactionOutput output: super.getOutputs()) Blockchain.putUTXOs(output.getHash(), output);
        //從鏈的UTXO清單將已使用掉的UTXO移出
        for(TransactionInput input: super.getInputs()) Blockchain.removeUTXOs(input.getSourceOutputHash());
    }

    public boolean processTransaction() {
        //讀入輸入源
        for(TransactionInput input: super.getInputs()) input.processAmount();
        //驗證交易簽名
        if(super.getSignature()!=null && verifiySignature() == false) {
            System.out.println("[x] 交易簽名驗證失敗"); //prompt
            return false;
        }
        //檢查交易金額是否合理
        if(super.getInputsAmount() < super.getAmount()) {
            System.out.println("[x] 交易金額不合理：" + super.getInputsAmount() + " -> " + super.getAmount()); //prompt
            return false;
        }
        //驗證交易Hash值
        if(super.getHash() == calculateHash()) {
            System.out.println("[x] 交易Hash值有誤"); //prompt
            return false;
        }
        payment();
        return true;
    }

    public String hashPlainData() {
        return (Integer.toString(payerId) +
                Integer.toString(receiverId) +
                super.getDetail() +
                Integer.toString(super.getAmount()) +
                Integer.toString(super.getSequence())
        );
    }
}
