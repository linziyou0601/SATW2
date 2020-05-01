package com.satw.demo.Controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Dao.CouponRepository;
import com.satw.demo.Dao.OrderRepository;
import com.satw.demo.Dao.ProductRepository;
import com.satw.demo.Model.Coupon;
import com.satw.demo.Model.Msg;
import com.satw.demo.Model.Order;
import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProductController {

    @Autowired
    NotificationController notificationController;
    
    @Autowired
    CouponRepository couponRepository;

    @Autowired
    ProductRepository productRepository;
    
    @Autowired
	OrderRepository orderRepository;
	
	@Autowired
    DataSource dataSource;
    
    //---------------------------------------所有產品---------------------------------------//
    //-------------------所有產品-------------------//
    @GetMapping("products")
    public String products(@RequestParam(value="key", required=false, defaultValue="") String key, Model model){
        List<Product> products = productRepository.findAllAvailable(key);
        Collections.reverse(products);
        model.addAttribute("key", key);
        model.addAttribute("products", products);
        return "products";
    }

    //-------------------產品資訊-------------------//
    @GetMapping("products/{id}")
    public String productDetail(@PathVariable int id, Model model){
        List<Product> products = productRepository.findById(id);
        if(products.size()>0){
            model.addAttribute("product", products.get(0));
            return "productDetail";
        } else {
            return "redirect:/products";
        }
    }
    
    //-------------------產品下訂-------------------//
    @PostMapping("requestOrder")
    @ResponseBody
    public Msg requestOrder(@RequestParam("id") int id, @RequestParam("quantity") int quantity, @RequestParam("couponCode") String couponCode, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<Product> products = productRepository.findById(id);
        if(user==null){
            return new Msg("Error", "Invalid operation!", "error");
        } else {
            Coupon coupon = null;
            if(!couponCode.equals("")){
                List<Coupon> coupons = couponRepository.findByCode(couponCode);
                if(coupons.size()>0){
                    coupon = coupons.get(0);
                    if(!coupon.getAvailable()){
                        coupon = null;
                        return new Msg("Failed", "Coupon is unavailable!", "warning");
                    }
                } else {
                    return new Msg("Failed", "Invalid Coupon code!", "warning");
                }
            }
            if(products.size()>0){
                Product product = products.get(0);
                if(product.getStockQty()>0){
                    if(quantity>0){
                        Order order = product.order(user, quantity, coupon);    //建立訂單
                        if(coupon!=null) coupon.setOrder(order);                //建立與Order的關聯
                        order = orderRepository.saveAndFlush(order);            //儲存訂單
                        product.minusStockQty(quantity);                        //減少庫存
                        productRepository.saveAndFlush(product);                //儲存商品
                        requestSendUnpaidNotify(order);                         //未付款通知
                        return new Msg("Successful", Integer.toString(order.getId()), "success");
                    } else {
                        return new Msg("Failed", "Quantity must large than 0.", "warning");
                    }
                } else {
                    return new Msg("Failed", "Product unavailable!", "warning");
                }
            } else {
                return new Msg("Error", "Invalid operation.", "error");
            }
        }
    }
    
    //-------------------發送訂單通知（未付款）-------------------//
    //Mediator
    public void requestSendUnpaidNotify(Order order){
        order.notifyUnpaidOrder(notificationController);
    }
}