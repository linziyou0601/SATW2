package com.satw.demo.Controller;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.satw.demo.Dao.UserRepository;
import com.satw.demo.Dao.WalletRepository;
import com.satw.demo.Model.User;
import com.satw.demo.Model.Wallet;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Controller
public class LinebotController {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    UserRepository userRepository;
    
    //---------------------------------------REST TEMPLATE POST REQUEST---------------------------------------//
    public Map<Object, Object> postRequest(String url, Map<?,?> data){
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Gson gson = new Gson();
            HttpEntity<String> entity = new HttpEntity<String>(gson.toJson(data), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return gson.fromJson(response.getBody(), new TypeToken<Map<Object, Object>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    //-1查不到,0未綁,1已綁,2非個人Channel
    //---------------------------------------BIND CHATBOT---------------------------------------//
    @GetMapping("linebot/bind/{channel_id}")
    public String bind(@PathVariable String channel_id, Model model){
        Map<Object, Object> requestData = new HashMap<>();
        requestData.put("channel_id", channel_id);
        Map<Object, Object> response = postRequest("https://satw2.linziyou.nctu.me:4567/getChannelBind", requestData);
        
        String[] tint_ary = {"帳號不存在", "未綁定", "已完成綁定", "群組不可綁定"};
        String[] class_ary = {"text-danger", "text-danger", "", "text-danger"};
        model.addAttribute("tint", tint_ary[Integer.parseInt((String)response.get("bind"))+1]);
        model.addAttribute("class", class_ary[Integer.parseInt((String)response.get("bind"))+1]);
        model.addAttribute("bind", response.get("bind"));
        return "linebotBind";
    }

    //---------------------------------------BIND CHATBOT---------------------------------------//
    @GetMapping("linebot/doBind/{channel_id}")
    public String doBind(@PathVariable String channel_id, HttpSession session){
        User user = (User)session.getAttribute("user");
        if(user==null) return "redirect:/login?redirect=/linebot/doBind/"+channel_id;
        Map<Object, Object> requestData = new HashMap<>();
        requestData.put("channel_id", channel_id);
        requestData.put("account", user.getAccount());
        postRequest("https://satw2.linziyou.nctu.me:4567/binding", requestData);
        return "redirect:/linebot/bind/"+channel_id;
    }

    //---------------------------------------CHATBOT PUSHING---------------------------------------//
    public void pushing(String title, String content, int order_id, String userWalletAddress){
        Wallet wallet = walletRepository.findFirstByAddress(userWalletAddress);
        int userId = wallet.getId();
        User user = userRepository.findFirstById(userId);
        String account = user.getAccount();
        Map<Object, Object> requestData = new HashMap<>();
        requestData.put("account", account);
        Map<Object, Object> response = postRequest("https://satw2.linziyou.nctu.me:4567/getChannelId", requestData);
        String channel_id = (String)response.get("channelId");
        if(!channel_id.equals("none")){
            /* {  
                "type": "flex",
                "title": "已出貨通知（測試）",
                "message": {  
                    "title": "已出貨通知（測試）",
                    "content": "賣家已將您的商品：「測試測試測試」出貨，請注意查收！",
                    "order_id": "1"
                },
                "channel_id": "U95caa80bb0e6031591886fc6c5c3a686",
                "template": "announcement"
            } */
            requestData = new HashMap<>();
            requestData.put("type", "flex");
            requestData.put("title", title);
            requestData.put("channel_id", channel_id);
            requestData.put("template", "announcement");
            Map<Object, Object> messageData = new HashMap<>();
            messageData.put("title", title);
            messageData.put("content", content);
            messageData.put("order_id", order_id);
            requestData.put("message", messageData);
            postRequest("https://satw2.linziyou.nctu.me:4567/pushing", requestData);
        }
    }
}