package com.satw.demo.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Dao.NotificationRepository;
import com.satw.demo.Model.Notification;
import com.satw.demo.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NotificationController {

    @Autowired
    OrderController orderController;

    @Autowired
    NotificationRepository notificationRepository;

	@Autowired
    DataSource dataSource;

    public void createNotify(String userWalletAddress, String txHash, int orderId, String type, String title, String description){
        Notification notify = new Notification(userWalletAddress, txHash, orderId, type, title, description);
        notificationRepository.save(notify);
        //Mediator
        if(orderId>0 && type.equals("Payment Paid")) orderController.requestUpdateOrderState(orderId);
    }

    @GetMapping("notification")
    public String notification(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/notification";
        List<Notification> notifications = notificationRepository.findByUserWalletAddress(user.getWalletAddress());
        Collections.reverse(notifications);
        model.addAttribute("notifications", notifications);
        return "notification";
    }

    @PostMapping("readNotification")
    @ResponseBody
    public void readNotification(@RequestBody Map<String,String> reqMap) {
        Notification notification = notificationRepository.findFirstById(Integer.parseInt(reqMap.get("id")));
        if(notification!=null){
            if(notification.getReaded()==false)
                notification.hasReaded();
            notificationRepository.saveAndFlush(notification);
        }
    }
}