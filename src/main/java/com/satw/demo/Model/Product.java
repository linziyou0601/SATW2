package com.satw.demo.Model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "product")
@EntityListeners(AuditingEntityListener.class)
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
    @Column(name="deleted")
    @Expose
    private boolean deleted;

    //Timestamp
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name = "createTime",  updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Expose
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
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
        this.deleted = false;
    }

    //Getter Setter
    public int getId(){ return id; }
    public User getSeller(){ return seller; }
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
    public int getPrice(){ return price; }
    public void setPrice(int price){ this.price = price; }
    public String getImgs(){ return imgs; }
    public void setImgs(String imgs){ this.imgs = imgs; }
    public int getStockQty(){ return stockQty; }
    public void setStockQty(int stockQty){ this.stockQty = stockQty; }
    public boolean getDeleted(){ return deleted; }
    public void setDeleted(boolean deleted){ this.deleted = deleted; }

    //Timestamp Getter Setter
    public Date getCreateTime(){ return createTime; }
    public Date getUpdateTime(){ return updateTime; }

    //Operator
    public void minusStockQty(int quantity){
        this.stockQty -= quantity;
    }
    public void addStockQty(int quantity){
        this.stockQty += quantity;
    }

    //Mediator
    public int getSellerId(){ return seller.getId(); }
    public String getSellerWalletAddress(){ return seller.getWalletAddress(); }
}