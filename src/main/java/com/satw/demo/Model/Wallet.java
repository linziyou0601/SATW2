package com.satw.demo.Model;

import static com.satw.demo.Util.KeyPairUtil.generateKeyPair;
import static com.satw.demo.Util.KeyPairUtil.keyToString;
import static com.satw.demo.Util.KeyPairUtil.publicKeyToAddress;

import java.security.Key;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.gson.annotations.Expose;
import com.satw.demo.Blockchain.Block;
import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Blockchain.Deposit;
import com.satw.demo.Blockchain.Payment;
import com.satw.demo.Blockchain.Transaction;
import com.satw.demo.Blockchain.TransactionInput;
import com.satw.demo.Blockchain.TransactionOutput;
import com.satw.demo.Blockchain.Withdraw;

@Entity
@Table(name = "wallet")
public class Wallet{
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;  

    //Object Attribute
    @Column(name="publicKey", columnDefinition="TEXT")
    @Expose
    private String publicKey;
    @Column(name="privateKey", columnDefinition="TEXT")
    @Expose
    private String privateKey;
    @Column(name="address", columnDefinition="TEXT")
    @Expose
    private String address;

    //Timestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createTime",  updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Expose
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updateTime",  updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Expose
    private Date updateTime;
    
    //Other DB's Relationships
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //------------------------------------Method------------------------------------
    //Constructor
    public Wallet(){
        Map<String, Key> result = generateKeyPair();
        this.privateKey = keyToString(result.get("privateKey"));
        this.publicKey = keyToString(result.get("publicKey"));
        this.address = publicKeyToAddress((PublicKey)result.get("publicKey"));
    }

    //Getter Setter
    public int getId(){
        return id;
    }
    public String getPublicKey(){
        return publicKey;
    }
    public void setPublic(String publicKey){
        this.publicKey = publicKey;
    }
    public String getPrivateKey(){
        return privateKey;
    }
    public void setPrivateKey(String privateKey){
        this.privateKey = privateKey;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }

    //Timestamp Getter Setter
    public Date getCreateTime(){
        return createTime;
    }
    public Date getUpdateTime(){
        return updateTime;
    }
    
    //Other DB's Relationships Setter
    public void setUser(User user){
        this.user = user;
    }

    //Operator
    public int getBalance(){
        Blockchain.updateChain();
        int total = 0;
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values())
            if(UTXO.verifyOwner(user.getWalletAddress()))
                total += UTXO.getAmount();
        return total;
    }
    public LinkedList<Transaction> getDetail(){
        LinkedList<Transaction> txs = new LinkedList<>();
        for(Block block: Blockchain.getChain())
            for(Transaction tx: block.getTransactions()){
                if((tx.getClassType().equals("Payment") && ((Payment)tx).getPayerAddress().equals(user.getWalletAddress())) ||
                   (tx.getClassType().equals("Payment") && ((Payment)tx).getReceiverAddress().equals(user.getWalletAddress())) ||
                   (tx.getClassType().equals("Withdraw") && ((Withdraw)tx).getPayerAddress().equals(user.getWalletAddress())) ||
                   (tx.getClassType().equals("Deposit") && ((Deposit)tx).getReceiverAddress().equals(user.getWalletAddress())))
                    txs.add(tx);
            }
        Collections.reverse(txs);
        return txs;
    }
    public Transaction deposit(int amount){
        Blockchain.updateChain();
        Transaction transaction = new Deposit(publicKey, user.getWalletAddress(), amount);
        transaction.generateSignature(privateKey);
        return transaction;
    }
    public Transaction withdraw(int amount){
        Blockchain.updateChain();
        boolean txCheck = true;
        LinkedList<TransactionInput> inputs = new LinkedList<>();
        int ownUTXOs = 0;

        //驗證餘額是否足夠本次交易，並更新UTXO
        //取得部分UTXO直到足夠支付本次交易的輸出
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values()){
            if(UTXO.verifyOwner(user.getWalletAddress())){
                inputs.add(new TransactionInput(UTXO.getHash()));
                ownUTXOs += UTXO.getAmount();
            }
            if(ownUTXOs > amount) break;
        }
        //驗證餘額是否足夠本次交易
        if(ownUTXOs < amount) {
            System.out.println("[x] 餘額不足，本次交易取消。"); //prompt
            txCheck = false;
        }

        //初步驗證無誤
        if(txCheck){
            Transaction transaction = new Withdraw(publicKey, user.getWalletAddress(), amount, inputs);
            transaction.generateSignature(privateKey);
            return transaction;
        }
        return null;
    }
    public Transaction payment(Order order){
        Blockchain.updateChain();
        boolean txCheck = true;
        LinkedList<TransactionInput> inputs = new LinkedList<>();
        int ownUTXOs = 0;
        String payerAddress = address;
        String receiverAddress = order.getState() instanceof Ordered? Blockchain.getThirdParty().getWalletAddress(): order.getProduct().getSeller().getWalletAddress();
        int amount = order.getState() instanceof Ordered? order.getPayableAmount(): order.getAmount();

        //驗證餘額是否足夠本次交易，並更新UTXO
        //取得部分UTXO直到足夠支付本次交易的輸出
        for(TransactionOutput UTXO: Blockchain.getUTXOs().values()){
            if(UTXO.verifyOwner(payerAddress)){
                inputs.add(new TransactionInput(UTXO.getHash()));
                ownUTXOs += UTXO.getAmount();
            }
            if(ownUTXOs > amount) break;
        }
        //驗證餘額是否足夠本次交易
        if(ownUTXOs < amount) {
            System.out.println("[x] 餘額不足，本次交易取消。"); //prompt
            txCheck = false;
        }

        //初步驗證無誤
        if(txCheck){
            String detail = order.getProduct().getTitle() + " ($" + order.getProduct().getPrice() + ") × " + order.getQuantity() + " - Discount($" + order.getCouponDiscount() + ") = " + order.getPayableAmount();
            Transaction transaction = new Payment(publicKey, payerAddress, receiverAddress, order.getId(), detail, amount, inputs);
            transaction.generateSignature(privateKey);
            return transaction;
        }
        return null;
    }
}