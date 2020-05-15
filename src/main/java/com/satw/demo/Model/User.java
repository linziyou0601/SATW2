package com.satw.demo.Model;

import java.util.LinkedList;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.satw.demo.Blockchain.Transaction;

import at.favre.lib.crypto.bcrypt.BCrypt;

@Entity
@Table(name = "user")
public class User{
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;

    //Object Attribute
    @Column(name="account", columnDefinition="TEXT")
    @Expose
    private String account;
    @Column(name="password", columnDefinition="TEXT")
    private String password;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "user")
    @Expose
    private Profile profile;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "user")
    private Wallet wallet;
    
    //Other DB's Relationships
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "seller")
    private Set<Product> products;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "buyer")
    private Set<Order> orders;

    //------------------------------------Method------------------------------------
    //Constructor
    public User(){
        this.wallet = new Wallet();
        this.wallet.setUser(this);
    }
    public User(String account, String password, Profile profile) {
        this.account = account;
        this.password = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, password.toCharArray());
        this.profile = profile;
        this.profile.setUser(this);
        this.wallet = new Wallet();
        this.wallet.setUser(this);
    }

    //Getter Setter
    public int getId(){ return id; }
    public String getAccount(){ return account; }
    public String getPassword(){ return password; }
    public void setPassword(String password){
        this.password = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, password.toCharArray());
    }
    public Profile getProfile(){ return profile; }
    public Wallet getWallet(){ return wallet; }

    //Operator
    public boolean verifyPassword(String password){
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), this.password);
        return result.verified;
    }
    public void editProfile(String name, String email, String address, String phone){
        this.profile.setName(name);
        this.profile.setEmail(email);
        this.profile.setAddress(address);
        this.profile.setPhone(phone);
    }
    
    //Mediator
    public String getProfileName(){ return profile.getName(); }
    public String getProfileAddress(){ return profile.getAddress(); }
    public String getProfileEmail(){ return profile.getEmail(); }
    //-
    public String getWalletPublicKey(){ return wallet.getPublicKey(); }
    public String getWalletPrivateKey(){ return wallet.getPrivateKey(); }
    public String getWalletAddress(){ return wallet.getAddress(); }
    public int getWalletBalance(){ return wallet.getBalance(); }
    public LinkedList<Transaction> getWalletDetail(){ return wallet.getDetail(); }
    //-
    public Transaction makeDeposit(int amount){ return wallet.deposit(amount); }
    public Transaction makeWithdraw(int amount){ return wallet.withdraw(amount); }
    public Transaction makePayment(Order order){ return wallet.payment(order); }
}