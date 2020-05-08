package com.satw.demo.Blockchain;

public class TransactionInput {
    private String sourceOutputHash;
    private String payerAddress;
    private int amount;
    public TransactionInput(String sourceOutputHash) {
        this.sourceOutputHash = sourceOutputHash;
    }
    // getter
    public String getSourceOutputHash() { return sourceOutputHash; }
    public String getPayerAddress() { return payerAddress; }
    public int getAmount(){ return amount; };

    // operator
    public void processAmount() {
        TransactionOutput UTXO = Blockchain.getUTXOs().get(sourceOutputHash);
        System.out.println(UTXO.getReceiverAddress());
        payerAddress = (UTXO==null? "": UTXO.getReceiverAddress());
        amount = (UTXO==null? 0: UTXO.getAmount());
    }
}