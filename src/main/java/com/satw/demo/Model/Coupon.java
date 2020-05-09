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

import com.google.gson.annotations.Expose;

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
    public int getDiscount(){
        return discount;
    }
    public boolean getAvailable(){
        return order == null;
    }

    //Other DB's Relationships Setter
    public void setOrder(Order order){
        this.order = order;
    }

    //Operator
}