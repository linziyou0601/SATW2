package com.satw.demo.Blockchain;

import java.util.LinkedList;

public class Payment extends Transaction {
    private String payerAddress;
    private String receiverAddress;
    private int orderId;
    //-------------------- 建構子、Getter、Setter --------------------
    public Payment(String publicKey, String payerAddress, String receiverAddress, int orderId, String detail, int amount, LinkedList<TransactionInput> inputs){
        super(publicKey, detail, amount, inputs, "Payment");
        this.payerAddress = payerAddress;
        this.receiverAddress = receiverAddress;
        this.orderId = orderId;
    }
    public String getPayerAddress(){ return payerAddress; }
    public String getReceiverAddress(){ return receiverAddress; }
    public int getOrderId(){ return orderId; }

    //交易處理
    private void payment(){
        Blockchain blockchain = Blockchain.getInstance();
        
        LinkedList<TransactionOutput> outputs = super.getOutputs();                                         //建立交易輸出金流
        outputs.add(new TransactionOutput(receiverAddress, super.getAmount(), super.getHash()));            //將新的UTXO放到鏈的UTXO清單中
        int restAmount = getInputsAmount() - super.getAmount();
        if(restAmount>0) outputs.add(new TransactionOutput(payerAddress, restAmount, super.getHash()));

        for(TransactionOutput output: super.getOutputs()) blockchain.putUTXOs(output.getHash(), output);    //將新的UTXO放到鏈的UTXO清單中
        for(TransactionInput input: super.getInputs()) blockchain.removeUTXOs(input.getSourceOutputHash()); //從鏈的UTXO清單將已使用掉的UTXO移出
    }

    //-------------------- concrete method --------------------
    public boolean processTransaction() {
        //讀入輸入源
        for(TransactionInput input: super.getInputs()) input.processAmount();
        //驗證交易簽名
        if(super.getSignature()!=null && verifySignature() == false) {
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
        return (payerAddress +
                receiverAddress +
                super.getDetail() +
                Integer.toString(super.getAmount()) +
                Integer.toString(super.getSequence())
        );
    }

    public boolean verifyOwner(String address){
        return this.payerAddress.equals(address) || this.receiverAddress.equals(address);
    }
}
