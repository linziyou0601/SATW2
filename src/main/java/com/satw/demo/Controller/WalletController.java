package com.satw.demo.Controller;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Blockchain.Transaction;
import com.satw.demo.Dao.OrderRepository;
import com.satw.demo.Model.Msg;
import com.satw.demo.Model.Order;
import com.satw.demo.Model.Ordered;
import com.satw.demo.Model.Otp;
import com.satw.demo.Model.User;

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
public class WalletController {
    
    @Autowired
	OrderRepository orderRepository;
	
	@Autowired
    DataSource dataSource;

    //---------------------------------------錢包---------------------------------------//
    @GetMapping("wallet")
    public String wallet(Model model, HttpSession session){
        if((User) session.getAttribute("user")==null) return "redirect:/login?redirect=/wallet";
        return "wallet";
    }
    
    //---------------------------------------付款頁---------------------------------------//
    @GetMapping("wallet/makePayment/{order_id}")
    public String makePayment(@PathVariable int order_id, Model model, HttpSession session, RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        Order order = orderRepository.findFirstById(order_id);
        if(user==null){
            attr.addFlashAttribute("alert", new Msg("Error", "Invalid operation!", "error"));
        } else {
            if(order!=null && order.getBuyerId() == user.getId() && order.getState() instanceof Ordered){
                model.addAttribute("order", order);
                return "makePayment";
            } else {
                attr.addFlashAttribute("alert", new Msg("Error", "Invalid operation!", "error"));
            }
        }
        return "redirect:/login?redirect=/myOrders/"+order_id;
    }

    //---------------------------------------付款---------------------------------------//
    @PostMapping("wallet/requestPayment")
    @ResponseBody
    public Msg requestPayment(@RequestParam("order_id") int order_id, HttpSession session){
        Msg msg = new Msg();
        User user = (User) session.getAttribute("user");
        Order order = orderRepository.findFirstById(order_id);
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            if(order!=null && order.getBuyerId() == user.getId() && order.getState() instanceof Ordered){
                Transaction tx = user.makePayment(order);
                msg = requestSendOtp(tx, user);
            } else {
                msg = new Msg("Error", "Invalid operation!", "error");
            }
        }
        return msg;
    }

    //---------------------------------------提款---------------------------------------//
    @PostMapping("wallet/requestWithdraw")
    @ResponseBody
    public Msg requestWithdraw(@RequestParam("amount") String amountStr, HttpSession session) {
        Msg msg = new Msg();
        User user = (User) session.getAttribute("user");
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            try {
                int amount = Integer.parseInt(amountStr);
                if(amount>0 && amount <= user.getWalletBalance()){
                    Transaction tx = user.makeWithdraw(amount);
                    msg = requestSendOtp(tx, user);
                } else {
                    msg = new Msg("Failed", "Your Amount Must Large Than 0 and Small or Equal to "+user.getWalletBalance(), "error");
                }
            } catch(NumberFormatException e){
                msg = new Msg("Error", "Invalid Number.", "error");
            }
        }
        return msg;
    }

    //---------------------------------------存款---------------------------------------//
    @PostMapping("wallet/requestDeposit")
    @ResponseBody
    public Msg requestDeposit(@RequestParam("amount") String amountStr, HttpSession session) {
        Msg msg;
        Blockchain blockchain = Blockchain.getInstance();
        User user = (User) session.getAttribute("user");
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            try {
                int amount = Integer.parseInt(amountStr);
                if(amount>0){
                    Transaction tx = user.makeDeposit(amount);
                    if(tx!=null){
                        blockchain.addUnveriedTransaction(tx);
                        msg = new Msg("Successful", "Your transaction "+tx.getDetail()+" has sent to blockchain", "success");
                    } else {
                        msg = new Msg("Failed", "It has some trubles.", "error");
                    }
                } else {
                    msg = new Msg("Failed", "Your Amount Can Not <= 0", "error");
                }
            } catch(NumberFormatException e){
                msg = new Msg("Error", "Invalid Number.", "error");
            }
        }
        return msg;
    }

    //---------------------------------------送出OTP---------------------------------------//
    public Msg requestSendOtp(Transaction tx, User user){
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        Otp otp = new Otp(user.getProfileEmail());
        if(tx!=null){
            if(otp.send()){
                session.setAttribute("newTx", tx);
                session.setAttribute("otp", otp);
                return new Msg("Successful", "Your transaction "+tx.getDetail()+" is waiting for OTP verification.", "success");
            } else {
                return new Msg("Failed", "Email sent failed, please check your email address.", "error");
            }
        } else {
            return new Msg("Failed", "Check if your balance is sufficient.", "error");
        }
    }
}