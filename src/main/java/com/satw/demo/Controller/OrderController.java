package com.satw.demo.Controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Blockchain.Transaction;
import com.satw.demo.Dao.OrderRepository;
import com.satw.demo.Dao.ProductRepository;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Model.Canceled;
import com.satw.demo.Model.Order;
import com.satw.demo.Model.Ordered;
import com.satw.demo.Model.Product;
import com.satw.demo.Model.Shipped;
import com.satw.demo.Model.User;
import com.satw.demo.Normal.Msg;
import com.satw.demo.Normal.CreateNotifyLambda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {
    
    @Autowired
    NotificationController notificationController;

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ProductRepository productRepository;
    
    @Autowired
	OrderRepository orderRepository;
	
	@Autowired
    DataSource dataSource;
    
    CreateNotifyLambda<String, String, Integer, String, String, String> createNotifyLambda = (userWalletAddress, txHash, orderId, type, title, description) -> {
        notificationController.createNotify(userWalletAddress, txHash, orderId, type, title, description);
    };

    //---------------------------------------我的訂單---------------------------------------//
    //-------------------我的訂單-------------------//
    @GetMapping("myOrders")
    public String myOrders(Model model, HttpSession session,  RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/myOrders";
        List<Order> orders = orderRepository.findByOwner(user);
        Collections.reverse(orders);
        model.addAttribute("orders", orders);
        return "myOrders";
    }
    
    //-------------------訂單明細-------------------//
    @GetMapping("myOrders/{id}")
    public String orderDetail(@PathVariable int id, Model model, HttpSession session, RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/myOrders/"+id;
        Order order = orderRepository.findFirstById(id);
        if(order!=null && (order.getBuyerId() == user.getId() || order.getProductSellerId() == user.getId())){
            model.addAttribute("order", order);
            return "orderDetail";
        } else {
            Msg msg = new Msg("Error", "Invalid operation!", "error");
            attr.addFlashAttribute("alert", msg);
            return "redirect:/myOrders";
        }
    }

    //---------------------------------------我的訂單---------------------------------------//
    //-------------------取消訂單-------------------//
    @PostMapping("requestCancelOrder")
    @ResponseBody
    public Msg requestCancelOrder(@RequestParam("id") int id, HttpSession session) {
        Msg msg = new Msg();
        User user = (User) session.getAttribute("user");
        Order order = orderRepository.findFirstById(id);
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            if(order!=null && (order.getBuyerId() == user.getId() || order.getProductSellerId() == user.getId()) && order.getState() instanceof Ordered){
                Product product = productRepository.findFirstByIdAndDeleted(order.getProductId(), false);
                product.addStockQty(order.getQuantity());
                productRepository.saveAndFlush(product);
                order.setState(new Canceled());
                orderRepository.saveAndFlush(order);
                msg = new Msg("Successful", "Order has canceled.", "success");
            } else {
                msg = new Msg("Error", "Invalid operation.", "error");
            }
        }
        return msg;
    }

    //-------------------完成訂單-------------------//
    @PostMapping("requestReceiveOrder")
    @ResponseBody
    public Msg requestReceiveOrder(@RequestParam("id") int id, HttpSession session) {
        Msg msg = new Msg();
        Blockchain blockchain = Blockchain.getInstance();
        User user = (User) session.getAttribute("user");
        Order order = orderRepository.findFirstById(id);
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            if(order!=null && order.getBuyerId() == user.getId() && order.getState() instanceof Shipped){
                User thirdParty = blockchain.getThirdParty();
                //第三方補差額
                while(thirdParty.getWalletBalance()<order.getAmount()){
                    Transaction tx = thirdParty.makeDeposit(order.getAmount() - thirdParty.getWalletBalance());
                    blockchain.addUnveriedTransaction(tx);
                }
                //第三方轉移Coins
                Transaction tx = thirdParty.makePayment(order);
                blockchain.addUnveriedTransaction(tx);
                blockchain.updateChain();
                msg = new Msg("Successful", "Order state has updated.", "success");
            } else {
                msg = new Msg("Error", "Invalid operation.", "error");
            }
        }
        return msg;
    }

    //-------------------更新訂單狀態-------------------//
    //Mediator
    @PostMapping("requestUpdateOrderState")
    @ResponseBody
    public Msg requestUpdateOrderState(@RequestParam("id") int id){
        Msg msg = new Msg();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        User user = (User) session.getAttribute("user");
        Order order = orderRepository.findFirstById(id);
        msg = order.updateState(createNotifyLambda, user);
        orderRepository.saveAndFlush(order);
        return msg;
    }
}