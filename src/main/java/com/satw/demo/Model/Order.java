package com.satw.demo.Model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.satw.demo.Controller.NotificationController;
import com.satw.demo.Controller.OrderController;

@Entity
@Table(name = "orders")
public class Order {    
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;

    //Object Attribute
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @Expose
    private Product product;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    @Expose
    private User buyer;
    @Column(name="quantity")
    @Expose
    private int quantity;
    @Transient
    private State state;
    @Column(name="productShot", columnDefinition="TEXT")
    @Expose
    private String productShot;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
    @Expose
    private Coupon coupon;

    //Timestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createTime",  updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Expose
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updateTime",  updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Expose
    private Date updateTime;

    //DB's Representing
    @Column(name="stateType", columnDefinition="TEXT")
    @Expose
    private String stateType;

    //------------------------------------Method------------------------------------
    //Constructor
    public Order(){}
    public Order(Product product, User buyer, int quantity, Coupon coupon) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        this.product = product;
        this.productShot = gson.toJson(product);
        this.buyer = buyer;
        this.quantity = quantity;
        this.coupon = coupon;
        this.setState(new Ordered());
    }

    //Getter Setter
    public int getId(){
        return id;
    }
    public Product getProduct(){
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(productShot, Product.class);
    }
    public void setProduct(Product product){
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        this.product = product;
        if(this.productShot==null) this.productShot = gson.toJson(product);
    }
    public User getBuyer(){
        return buyer;
    }
    public void setBuyer(User buyer){
        this.buyer = buyer;
    }
    public int getQuantity(){
        return quantity;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    public String getStateType(){
        return stateType;
    }
    public void setStateType(String stateType){
        this.stateType = stateType;
        try {
            Class<?> clazz = Class.forName("com.satw.demo.Model."+stateType);
            this.state = (State) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    public State getState(){
        setStateType(this.stateType);
        return state;
    }
    public void setState(State state){
        this.state = state;
        this.stateType = state.getStateType();
    }
    public Coupon getCoupon(){
        return coupon;
    }
    public void setCoupon(Coupon coupon){
        this.coupon = coupon;
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
    public Msg updateState(OrderController orderController, User loginUser) {
        setStateType(this.stateType);
		return state.update(this, orderController, loginUser);
    }
    public int getAmount(){
        return getProduct().getPrice() * quantity;
    }
    public int getPayableAmount(){
        int discount = coupon==null? 0: coupon.getDiscount();
        return Math.max(0, getProduct().getPrice() * quantity - discount);
    }
    public String getDetail(){
        String s = getProduct().getTitle()+" ($" + getProduct().getPrice() + ") * " + quantity + "\n";
        s += "Amount = " + getAmount() + "\n";
        s += "Discount = -" + getCouponDiscount() + "\n";
        s += "Payable = " + getPayableAmount();
        return s;
    }
    public void notifyUnpaidOrder(NotificationController notificationController){
        notificationController.createNotify(buyer.getId(), "", id, "Unpaid Order", "Unpaid Order", getDetail());
    }
    public void notifyUnshipOrder(NotificationController notificationController){
        notificationController.createNotify(product.getSeller().getId(), "", id, "Unship Order", "Unship Order", getDetail());
    }
    public void sendOrderDetail(){

    }

    //Mediator
    public int getProductId(){
        return getProduct().getId();
    }
    public User getProductSeller(){
        return getProduct().getSeller();
    }
    public String getProductTitle(){
        return getProduct().getTitle();
    }
    public String getProductDescription(){
        return getProduct().getDescription();
    }
    public int getProductPrice(){
        return getProduct().getPrice();
    }
    public String getProductImgs(){
        return product.getImgs();
    }
    public String getCouponCode(){
        return coupon==null? "": coupon.getCode();
    }
    public int getCouponDiscount(){
        return coupon==null? 0: coupon.getDiscount();
    }
}