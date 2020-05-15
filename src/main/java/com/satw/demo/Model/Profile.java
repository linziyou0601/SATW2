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
@Table(name = "profile")
public class Profile{
    //------------------------------------DB Columns------------------------------------
    //Primary Key
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Expose
    private int id;  

    //Object Attribute
    @Column(name="name", columnDefinition="TEXT")
    @Expose
    private String name;
    @Column(name="email", columnDefinition="TEXT")
    @Expose
    private String email;
    @Column(name="address", columnDefinition="TEXT")
    @Expose
    private String address;
    @Column(name="phone", columnDefinition="TEXT")
    @Expose
    private String phone;
    
    //Other DB's Relationships
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //------------------------------------Method------------------------------------
    //Constructor
    public Profile(){}
    public Profile(String name, String email, String address, String phone) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
    }

    //Getter Setter
    public int getId(){ return id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
    public String getAddress(){ return address; }
    public void setAddress(String address){ this.address = address; }
    public String getPhone(){ return phone; }
    public void setPhone(String phone){ this.phone = phone; }
    
    //Other DB's Relationships Setter
    public void setUser(User user){ this.user = user; }
}