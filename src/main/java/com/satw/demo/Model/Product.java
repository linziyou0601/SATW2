package com.satw.demo.Model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.gson.annotations.Expose;

@Entity
@Table(name = "product")
public class Product{
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;

    //Object Attribute
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @Expose
    private User seller;
    @Column(name="title", columnDefinition="TEXT")
    @Expose
    private String title;
    @Column(name="description", columnDefinition="TEXT")
    @Expose
    private String description;
    @Column(name="price")
    @Expose
    private int price;
    @Column(name="imgs", columnDefinition="TEXT")
    @Expose
    private String imgs;
    @Column(name="stockQty")
    @Expose
    private int stockQty;

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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "product")
    private Set<Order> orders;

    //------------------------------------Method------------------------------------
    //Constructor
    public Product(){}
    public Product(User seller, String title, String description, int price, String imgs, int stockQty) {
        this.seller = seller;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imgs = imgs;
        this.stockQty = stockQty;
    }

    //Getter Setter
    public int getId(){
        return id;
    }
    public User getSeller(){
        return seller;
    }
    public void setSeller(User seller){
        this.seller = seller;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public int getPrice(){
        return price;
    }
    public void setPrice(int price){
        this.price = price;
    }
    public String getImgs(){
        return imgs;
    }
    public void setImgs(String imgs){
        this.imgs = imgs;
    }
    public int getStockQty(){
        return stockQty;
    }
    public void setStockQty(int stockQty){
        this.stockQty = stockQty;
    }

    //Timestamp Getter Setter
    public Date getCreateTime(){
        return createTime;
    }
    public Date getUpdateTime(){
        return updateTime;
    }
    
    //Other DB's Relationships Setter

    //Operator
    public Order order(User buyer, int quantity, Coupon coupon){
        Order order = new Order(this, buyer, quantity, coupon);
        return order;
    }
    public void minusStockQty(int quantity){
        this.stockQty -= quantity;
    }
    public void addStockQty(int quantity){
        this.stockQty += quantity;
    }
}