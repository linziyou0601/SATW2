package com.satw.demo.Controller;

import java.util.List;

import javax.sql.DataSource;

import com.satw.demo.Dao.ProductRepository;
import com.satw.demo.Model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    @Autowired
    NotificationController notificationController;

    @Autowired
    ProductRepository productRepository;
	
	@Autowired
    DataSource dataSource;

    //---------------------------------------所有產品---------------------------------------//
    //-------------------所有產品-------------------//
    @GetMapping("products")
    public String products(@RequestParam(value="key", required=false, defaultValue="") String key, Model model){
        List<Product> products = productRepository.findAllAvailable(key);
        model.addAttribute("key", key);
        model.addAttribute("products", products);
        return "products";
    }

    //-------------------產品資訊-------------------//
    @GetMapping("products/{id}")
    public String productDetail(@PathVariable int id, Model model){
        Product product = productRepository.findFirstByIdAndDeleted(id, false);
        if(product!=null){
            model.addAttribute("product", product);
            return "productDetail";
        } else {
            return "redirect:/products";
        }
    }
}