package com.satw.demo.Model;

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

import org.springframework.lang.Nullable;

import java.util.Date;

@Entity
@Table(name = "coupon")
public class Coupon {
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;

    //Object Attribute
    @Column(name="code", columnDefinition="TEXT")
    @Expose
    private String code;
    @Column(name="discount")
    @Expose
    private int discount;

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
    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    //------------------------------------Method------------------------------------
    //Constructor
    public Coupon(){}
    public Coupon(String code, int discount){
        this.code = code;
        this.discount = discount;
    }

    //Getter Setter
    public int getId(){
        return id;
    }
    public String getCode(){
        return code;
    }
    public void setCode(String code){
        this.code = code;
    }
    public int getDiscount(){
        return discount;
    }
    public void setDiscount(int discount){
        this.discount = discount;
    }
    public boolean getAvailable(){
        return order == null;
    }

    //Timestamp Getter Setter
    public Date getCreateTime(){
        return createTime;
    }
    public Date getUpdateTime(){
        return updateTime;
    }

    //Other DB's Relationships Setter
    public Order getOrder(){
        return order;
    }
    public void setOrder(Order order){
        this.order = order;
    }

    //Operator
}