package com.satw.demo.Controller;

import static com.satw.demo.Util.StringUtil.generateCouponCode;
import static com.satw.demo.Util.StringUtil.generateCouponDiscount;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.satw.demo.Dao.CouponRepository;
import com.satw.demo.Dao.OrderRepository;
import com.satw.demo.Dao.ProductRepository;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Dao.WalletRepository;
import com.satw.demo.Model.Coupon;
import com.satw.demo.Model.Order;
import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiController {

    @Autowired
    CouponRepository couponRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    WalletRepository walletRepository;
    
    @Autowired
    ProductRepository productRepository;
	
    @Autowired
    OrderRepository orderRepository;

	@Autowired
    DataSource dataSource;

    //---------------------------------------COUPON GENERATOR---------------------------------------//
    @GetMapping("api/generateCoupon")
    @ResponseBody
    public Map<Object, Object> generateCoupon() {
        String code = generateCouponCode();
        int discount = generateCouponDiscount();
        List<Coupon> coupons = couponRepository.findByCode(code);
        while(coupons.size()>0){
            code = generateCouponCode();
            coupons = couponRepository.findByCode(code);
        }
        Coupon coupon = new Coupon(code, discount);
        couponRepository.save(coupon);
        Map<Object, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("discount", discount);
        return result;
    }

    //---------------------------------------GET COUPON---------------------------------------//
    @GetMapping("api/getCoupon")
    @ResponseBody
    public Map<Object, Object> getCoupon(@RequestParam("code") String code) {
        List<Coupon> coupons = couponRepository.findByCode(code);
        Map<Object, Object> result = new HashMap<>();
        if(coupons.size()>0){
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Coupon coupon = coupons.get(0);
            result.put("status", "successful");
            result.put("coupon", gson.fromJson(gson.toJson(coupon), Object.class));
            result.put("available", coupon.getAvailable());
        } else {
            result.put("status", "failed");
            result.put("coupon", "");
            result.put("available", false);
        }
        return result;
    }

    //---------------------------------------GET USER ID---------------------------------------//
    @GetMapping("api/getUserId")
    @ResponseBody
    public Map<Object, Object> getUserId(@RequestParam("account") String account) {
        List<User> users = userRepository.findByAccount(account);
        Map<Object, Object> result = new HashMap<>();
        if(users.size()>0){
            result.put("status", "successful");
            result.put("user_id", users.get(0).getId());
        } else {
            result.put("status", "failed");
            result.put("user_id", "");
        }
        return result;
    }

    //---------------------------------------GET BALANCE---------------------------------------//
    @GetMapping("api/getBalance")
    @ResponseBody
    public Map<Object, Object> getBalance(@RequestParam("user_id") int user_id) {
        List<User> users = userRepository.findById(user_id);
        Map<Object, Object> result = new HashMap<>();
        if(users.size()>0){
            result.put("status", "successful");
            result.put("balance", users.get(0).getWalletBalance());
        } else {
            result.put("status", "failed");
            result.put("balance", "");
        }
        return result;
    }

    //---------------------------------------GET MY PRODUCTS---------------------------------------//
    @GetMapping("api/getMyProducts")
    @ResponseBody
    public Map<Object, Object> getMyProducts(@RequestParam("user_id") int user_id) {
        List<User> users = userRepository.findById(user_id);
        Map<Object, Object> result = new HashMap<>();
        if(users.size()>0){
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            List<Product> products = productRepository.findBySeller(users.get(0));
            result.put("status", "successful");
            result.put("my_products", gson.fromJson(gson.toJson(products), Object.class));
        } else {
            result.put("status", "failed");
            result.put("my_products", new ArrayList<Product>());
        }
        return result;
    }

    //---------------------------------------GET MY ORDERS---------------------------------------//
    @GetMapping("api/getMyOrders")
    @ResponseBody
    public Map<Object, Object> getMyOrders(@RequestParam("user_id") int user_id) {
        List<User> users = userRepository.findById(user_id);
        Map<Object, Object> result = new HashMap<>();
        if(users.size()>0){
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            List<Order> orders = orderRepository.findByOwner(users.get(0));
            result.put("status", "successful");
            result.put("my_orders", gson.fromJson(gson.toJson(orders), Object.class));
        } else {
            result.put("status", "failed");
            result.put("my_orders", new ArrayList<Order>());
        }
        return result;
    }

    //---------------------------------------GET PRODUCTS---------------------------------------//
    @GetMapping("api/getProducts")
    @ResponseBody
    public Map<Object, Object> getMyProducts(@RequestParam(value="key", required=false, defaultValue="") String key) {
        Map<Object, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        List<Product> products = productRepository.findAllAvailable(key);
        result.put("status", "successful");
        result.put("products", gson.fromJson(gson.toJson(products), Object.class));
        return result;
    }

    //---------------------------------------GET IMAGES---------------------------------------//
    @GetMapping(value = "/uploads/{filename}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] pixelTracking(@PathVariable String filename) {
        byte[] bytes = new byte[1];
        String UPLOADED_FOLDER = System.getProperty("user.home") + File.separator + "uploads";

        try{
            File file = new File(UPLOADED_FOLDER, filename);
            FileInputStream fileInputStreamReader = new FileInputStream(new File(file.getAbsolutePath()));
            bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            fileInputStreamReader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}