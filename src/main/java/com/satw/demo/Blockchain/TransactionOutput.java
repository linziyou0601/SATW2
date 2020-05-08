package com.satw.demo.Blockchain;

import static com.satw.demo.Util.StringUtil.*;

public class TransactionOutput {
    private String hash;                  //TXO id
    private String parentTxHash;          //This time Tx parent id
    private String receiverAddress;
    private int amount;
    public TransactionOutput(String receiverAddress, int amount, String parentTxHash) {
        this.receiverAddress = receiverAddress;
        this.amount = amount;
        this.parentTxHash = parentTxHash;
        this.hash = SHA256(receiverAddress + Integer.toString(amount) + parentTxHash);
    }
    // getter
    public String getHash(){ return hash; }
    public String getParentTxId(){ return parentTxHash; }
    public String getReceiverAddress(){ return receiverAddress; }
    public int getAmount(){ return amount; }

    // operator
    public boolean verifyOwner(String receiverAddress) { 
        return this.receiverAddress.equals(receiverAddress);
    }
}