package com.satw.demo.Blockchain;

import static com.satw.demo.Util.StringUtil.*;

public class TransactionOutput {
    private String hash;                  //TXO id
    private String parentTxHash;          //This time Tx parent id
    private int receiverId;
    private int amount;
    public TransactionOutput(int receiverId, int amount, String parentTxHash) {
        this.receiverId = receiverId;
        this.amount = amount;
        this.parentTxHash = parentTxHash;
        this.hash = SHA256(receiverId + Integer.toString(amount) + parentTxHash);
    }
    // getter
    public String getHash(){ return hash; }
    public String getParentTxId(){ return parentTxHash; }
    public int getReceiverId(){ return receiverId; }
    public int getAmount(){ return amount; }

    // operator
    public boolean verifyOwner(int receiverId) { return this.receiverId == receiverId; }
}