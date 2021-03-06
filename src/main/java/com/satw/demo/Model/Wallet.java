package com.satw.demo.Model;

import static com.satw.demo.Util.KeyPairUtil.generateKeyPair;
import static com.satw.demo.Util.KeyPairUtil.keyToString;
import static com.satw.demo.Util.KeyPairUtil.publicKeyToAddress;

import java.security.Key;
import java.security.PublicKey;
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
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Blockchain.Deposit;
import com.satw.demo.Blockchain.Payment;
import com.satw.demo.Blockchain.Transaction;
import com.satw.demo.Blockchain.TransactionInput;
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
    
    //Other DB's Relationships
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //ACCESS POINT OF SINGLETON BLOCKCHAIN INSTANCE
    @Transient
    Blockchain blockchain = Blockchain.getInstance();

    //------------------------------------Method------------------------------------
    //Constructor
    public Wallet(){
        Map<String, Key> result = generateKeyPair();
        this.privateKey = keyToString(result.get("privateKey"));
        this.publicKey = keyToString(result.get("publicKey"));
        this.address = publicKeyToAddress((PublicKey)result.get("publicKey"));
    }

    //Getter Setter
    public int getId(){ return id; }
    public String getPublicKey(){ return publicKey; }
    public String getPrivateKey(){ return privateKey; }
    public String getAddress(){ return address; }
    
    //Other DB's Relationships Setter
    public void setUser(User user){ this.user = user; }

    //Operator
    public int getBalance(){ return blockchain.getBalance(address); }
    public LinkedList<Transaction> getTransactions(){ return blockchain.getTransactions(address); }
    public Transaction deposit(int amount){
        Transaction transaction = new Deposit(publicKey, address, amount);
        transaction.generateSignature(privateKey);
        return transaction;
    }
    public Transaction withdraw(int amount){
        //驗證餘額是否足夠本次交易
        if(getBalance() >= amount){
            LinkedList<TransactionInput> inputs = blockchain.getTxInputs(address, amount);
            Transaction transaction = new Withdraw(publicKey, address, amount, inputs);
            transaction.generateSignature(privateKey);
            return transaction;
        }
        return null;
    }
    public Transaction payment(Order order){
        String receiverAddress = order.getState() instanceof Ordered? blockchain.getThirdPartyWalletAddress(): order.getProductSellerWalletAddress();
        int amount = order.getState() instanceof Ordered? order.getPayableAmount(): order.getAmount();

        //驗證餘額是否足夠本次交易
        if(getBalance() >= amount){
            LinkedList<TransactionInput> inputs = blockchain.getTxInputs(address, amount);
            String detail = order.getDetail();
            Transaction transaction = new Payment(publicKey, address, receiverAddress, order.getId(), detail, amount, inputs);
            transaction.generateSignature(privateKey);
            return transaction;
        }
        return null;
    }
}