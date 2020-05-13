package com.satw.demo.Controller;

import javax.servlet.http.HttpSession;

import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Blockchain.Transaction;
import com.satw.demo.Model.Msg;
import com.satw.demo.Model.Otp;
import com.satw.demo.Model.Otp.ValidState;
import com.satw.demo.Model.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OtpController {

    //---------------------------------------OTP頁---------------------------------------//
    @GetMapping("wallet/otp")
    public String otpAuthentication(Model model, HttpSession session){
        if(session.getAttribute("user")==null) return "redirect:/login?redirect=/wallet/otp";
        if(session.getAttribute("otp")==null) return "redirect:/";
        model.addAttribute("tx", session.getAttribute("newTx"));
        return "otp";
    }
    
    //---------------------------------------驗證OTP---------------------------------------//
    @PostMapping("wallet/requestVerifyOtp")
    @ResponseBody
    public Msg requestVerifyOtp(@RequestParam("otpCode") String otpCode, HttpSession session) {
        Msg msg = new Msg();
        User user = (User) session.getAttribute("user");
        Otp otp = (Otp) session.getAttribute("otp");
        if(user==null){
            msg = new Msg("Error", "Invalid operation!", "error");
        } else {
            if(otp==null){
                msg = new Msg("Error", "Invalid operation.", "error");
            } else {
                ValidState state = otp.verifyOtp(otpCode);
                switch(state){
                    case CORRECT:
                        Transaction tx = (Transaction) session.getAttribute("newTx");
                        Blockchain.addUnveriedTransaction(tx);
                        msg = new Msg("Successful", "Your transaction "+tx.getDetail()+" has sent to blockchain.", "success");
                        session.removeAttribute("newTx");
                        session.removeAttribute("otp");
                        break;
                    case OVERTRIES:
                        msg = new Msg("Error", "Retry over 3 times. Canceling the transaction.", "error");
                        session.removeAttribute("newTx");
                        session.removeAttribute("otp");
                        break;
                    case EXPIRED:
                        msg = new Msg("Failed", "Expired otp. Reseding the new one to your email.", "warning");
                        session.setAttribute("otp", otp);
                        break;
                    case INCORRECT:
                        msg = new Msg("Failed", "Incorrect otp.", "warning");
                        session.setAttribute("otp", otp);
                        break;
                }
            }
        }
        return msg;
    }
}