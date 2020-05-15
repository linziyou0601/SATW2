package com.satw.demo.Controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Blockchain.Blockchain;
import com.satw.demo.Dao.ProfileRepository;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Model.Msg;
import com.satw.demo.Model.Profile;
import com.satw.demo.Model.User;
import com.satw.demo.Util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ProfileRepository profileRepository;
	
	@Autowired
    DataSource dataSource;

    //---------------------------------------註冊頁---------------------------------------//
    @GetMapping("register")
    public String register(HttpSession session,  RedirectAttributes attr){
        if(session.getAttribute("user")!=null) return "redirect:/";
        return "register";
    }

    @PostMapping("/requestRegister")
    @ResponseBody
    public Msg requestRegister(@RequestParam("account") String account,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               @RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam("address") String address,
                               @RequestParam("phone") String phone,
                               HttpSession session
    ) {
        Msg msg = new Msg();
        List<User> users = userRepository.findByAccount(account);
        List<Profile> profiles = profileRepository.findByEmail(email);
        //驗證帳戶是否重複、密碼是否重複、Email是否重複
        if(users.size()>0)
            msg = new Msg("Failed", "Account is Exists!", "error");
        else if(profiles.size()>0)
            msg = new Msg("Failed", "Email is Used!", "error");
        else if(name.equals("") || email.equals("") || address.equals(""))
            msg = new Msg("Failed", "Please check if the field is fill.!", "warning");
        else if(!StringUtil.validateEmail(email))
            msg = new Msg("Failed", "Email is Invalid!", "error");
        else if(!password.equals(confirmPassword))
            msg = new Msg("Failed", "Password is Not Equals!", "error");
        else {
            //建立帳戶
            Profile profile = new Profile(name, email, address, phone);
            User register = new User(account, password, profile);                                                                                            //外鍵
            userRepository.save(register);  
            msg = new Msg("Successful", "Account is created", "success");                                                                                        //儲存
        }
        
        return msg;
    }

    //---------------------------------------登入登出---------------------------------------//
    @GetMapping("login")
    public String login(@RequestParam(value="redirect", required=false, defaultValue="") String redirect, Model model, HttpSession session){
        if(redirect.equals("")) redirect = "/";
        if(session.getAttribute("user")!=null) return "redirect:"+redirect;
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/requestLogin")
    public String requestLogin(@RequestParam("redirect") String redirect, 
                               @RequestParam("account") String account,
                               @RequestParam("password") String password,  
                               HttpSession session,  
                               RedirectAttributes attr){
        if(redirect.equals("")) redirect = "/";
        if(session.getAttribute("user")!=null) return "redirect:"+redirect;
        Msg msg = new Msg();
        User user = userRepository.findFirstByAccount(account);
        if(user!=null){
            if(user.verifyPassword(password)){
                session.setAttribute("user", user);
                Blockchain.updateChain();
            } else {
                msg = new Msg("Failed", "Incorrect Password!", "error");
            }
        } else {
            msg = new Msg("Failed", "Account Nou Found!", "error");
        }
        attr.addFlashAttribute("alert", msg);
        return "redirect:/login"+"?redirect="+redirect;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("user");
        session.invalidate();
        return "redirect:/login";
    }
}