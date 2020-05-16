package com.satw.demo.Controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.satw.demo.Dao.ProfileRepository;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Model.Profile;
import com.satw.demo.Model.User;
import com.satw.demo.Normal.Msg;
import com.satw.demo.Util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManageProfileController {

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ProfileRepository profileRepository;
	
	@Autowired
    DataSource dataSource;

    //---------------------------------------個人檔案---------------------------------------//
    @GetMapping("editProfile")
    public String editProfile(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/editProfile";
        model.addAttribute("profile", user.getProfile());
        return "editProfile";
    }

    @PostMapping("requestEditProfile")
    public String requestEditProfile(@ModelAttribute Profile profile, HttpSession session, RedirectAttributes attr){
        User user = (User) session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/editProfile";
        Msg msg = new Msg();
        List<Profile> profiles = profileRepository.findByEmail(profile.getEmail());
        
        if(profiles.size()>0 && profiles.get(0).getId()!=user.getProfile().getId()){
            //驗證Email是否重複
            msg = new Msg("Failed", "Email is Used!", "error");
            attr.addFlashAttribute("alert", msg);
        } else if(profile.getName().equals("") || profile.getEmail().equals("") || profile.getAddress().equals("")){
            msg = new Msg("Failed", "Please check if the field is fill.!", "warning");
            attr.addFlashAttribute("alert", msg);
        } else if(!StringUtil.validateEmail(profile.getEmail())) {
            msg = new Msg("Failed", "Email is Invalid!", "error");
            attr.addFlashAttribute("alert", msg);
        } else{
            //修改資料
            user.editProfile(profile.getName(), profile.getEmail(), profile.getAddress(), profile.getPhone());
            userRepository.save(user);
            session.setAttribute("user", user);
            msg = new Msg("Successful", "Profile edited!", "success");
            attr.addFlashAttribute("alert", msg);
        }
        return "redirect:/editProfile";
    }
}