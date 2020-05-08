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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.favre.lib.crypto.bcrypt.BCrypt;

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
    public String register(Model model, HttpSession session,  RedirectAttributes attr){
        if(session.getAttribute("user")!=null) return "redirect:/";
        model.addAttribute("register", new User());
        model.addAttribute("profile", new Profile());
        return "register";
    }

    @PostMapping("/requestRegister")
    @ResponseBody
    public Msg requestRegister(@ModelAttribute User register,
                                 @ModelAttribute Profile profile,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session
    ) {
        Msg msg = new Msg();
        List<User> users = userRepository.findByAccount(register.getAccount());
        List<Profile> profiles = profileRepository.findByEmail(profile.getEmail());
        //驗證帳戶是否重複、密碼是否重複、Email是否重複
        if(users.size()>0)
            msg = new Msg("Failed", "Account is Exists!", "error");
        else if(profiles.size()>0)
            msg = new Msg("Failed", "Email is Used!", "error");
        else if(profile.getName().equals("") || profile.getEmail().equals("") || profile.getAddress().equals(""))
            msg = new Msg("Failed", "Please check if the field is fill.!", "warning");
        else if(!StringUtil.validateEmail(profile.getEmail()))
            msg = new Msg("Failed", "Email is Invalid!", "error");
        else if(!register.getPassword().equals(confirmPassword))
            msg = new Msg("Failed", "Password is Not Equals!", "error");
        else {
            //建立帳戶
            register.setPassword(BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, register.getPassword().toCharArray()));    //密碼加密 php_hash加密
            register.setProfile(profile);
            profile.setUser(register);                                                                                              //外鍵
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
        List<User> users = userRepository.findByAccount(account);
        if(users.size()>0){
            User user = users.get(0);
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

    //-------------------取得第三方使用者-------------------//
    //Mediator
    public User getThirdParty(){
        List<User> users = userRepository.findById(1);
        return users.get(0);
    }
}