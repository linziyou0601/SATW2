package com.satw.demo.Model;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import com.satw.demo.Controller.NotificationController;
import com.satw.demo.Controller.OrderController;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
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
    @Column(name="price")
    @Expose
    private int price;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
    @Expose
    private Coupon coupon;
    @Transient
    private State state;

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

    //DB's Representing
    @Column(name="state_type", columnDefinition="TEXT")
    @Access(AccessType.PROPERTY)
    @Expose
    private String stateType;

    //------------------------------------Method------------------------------------
    //Constructor
    public Order(){}
    public Order(Product product, User buyer, int quantity, Coupon coupon) {
        this.product = product;
        this.buyer = buyer;
        this.price = product.getPrice();
        this.quantity = quantity;
        this.coupon = coupon;
        this.setState(new Ordered());
    }

    //Getter Setter
    public int getId(){ return id; }
    public Product getProduct(){ return this.product; }
    public User getBuyer(){ return buyer; }
    public int getPrice(){ return price; }
    public int getQuantity(){ return quantity; }
    public Coupon getCoupon(){ return coupon; }
    public State getState(){ return state; }
    public void setState(State state){
        this.state = state;
        this.stateType = state.getType();
    }

    //Timestamp Getter Setter
    public Date getCreateTime(){ return createTime; }
    public Date getUpdateTime(){ return updateTime; }
    
    //Other DB's Relationships Setter
    public String getStateType(){ return stateType; }
    public void setStateType(String stateType){
        this.stateType = stateType;
        setStateFromStateType();
    }
    public void setStateFromStateType(){
        try {
            Class<?> clazz = Class.forName("com.satw.demo.Model."+stateType);
            this.state = (State) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    //Operator
    public Msg updateState(OrderController orderController, User loginUser) {
		return state.update(this, orderController, loginUser);
    }
    public int getAmount(){
        return price * quantity;
    }
    public int getPayableAmount(){
        int discount = coupon==null? 0: coupon.getDiscount();
        return Math.max(0, price * quantity - discount);
    }
    public String getDetail(){
        String s = product.getTitle()+" ($" + price + ") * " + quantity + "\n";
        s += "Amount = " + getAmount() + "\n";
        s += "Discount = -" + getCouponDiscount() + "\n";
        s += "Payable = " + getPayableAmount();
        return s;
    }
    public void notifyUnpaidOrder(NotificationController notificationController){
        notificationController.createNotify(buyer.getWalletAddress(), "", id, "Unpaid Order", "Unpaid Order", getDetail());
    }
    public void notifyUnshipOrder(NotificationController notificationController){
        notificationController.createNotify(getProductSellerWalletAddress(), "", id, "Unship Order", "Unship Order", getDetail());
    }
    public void sendOrderDetail(){}

    //Mediator
    //product
    public int getProductId(){ return product.getId(); }
    public String getProductTitle(){ return product.getTitle(); }
    public String getProductDescription(){ return product.getDescription(); }
    public int getProductPrice(){ return price; }
    public String getProductImgs(){ return product.getImgs(); }
    public User getProductSeller(){ return product.getSeller(); }
    public int getProductSellerId(){ return product.getSellerId(); }
    public String getProductSellerWalletAddress(){ return product.getSellerWalletAddress(); }
    //buyer
    public int getBuyerId(){ return buyer.getId(); }
    public String getBuyerWalletAddress(){ return buyer.getWalletAddress(); }
    //coupon
    public String getCouponCode(){ return coupon==null? "": coupon.getCode(); }
    public int getCouponDiscount(){ return coupon==null? 0: coupon.getDiscount(); }
}