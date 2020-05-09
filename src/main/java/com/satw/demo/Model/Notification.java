package com.satw.demo.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification {
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    //Object Attribute
    @Column(name="userWalletAddress", columnDefinition="TEXT")
    private String userWalletAddress;
    @Column(name="txHash", columnDefinition="TEXT")
    private String txHash;
    @Column(name="orderId")
    private int orderId;
    @Column(name="type", columnDefinition="TEXT")
    private String type;
    @Column(name="title", columnDefinition="TEXT")
    private String title;
    @Column(name="description", columnDefinition="TEXT")
    private String description;
    @Column(name="readed")
    private boolean readed;

    //------------------------------------Method------------------------------------
    //Constructor
    public Notification(){}
    public Notification(String userWalletAddress, String txHash, int orderId, String type, String title, String description) {
        this.userWalletAddress = userWalletAddress;
        this.txHash = txHash;
        this.orderId = orderId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.readed = false;
    }

    //Getter Setter
    public int getId(){
        return id;
    }
    public String getUserWalletAddress(){
        return userWalletAddress;
    }
    public String getTxHash(){
        return txHash;
    }
    public int getOrderId(){
        return orderId;
    }
    public String getType(){
        return type;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public boolean getReaded(){
        return readed;
    }
    public void setReaded(boolean readed){
        this.readed = readed;
    }

    //Operator
}