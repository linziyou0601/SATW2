package com.satw.demo.Util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {
    public static boolean sendEmail(String otpCode, String email) {
        boolean result = false;
        String host = "smtp.gmail.com";
        int port = 587;
        final String username = "<domain>@gmail.com";
        final String password = "<passowrd>";
      
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", port);
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("b10723007@gemail.yuntech.edu.tw"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("SATW2 Transaction OTP Code");
            message.setText("Your otp code is: "+ otpCode);
      
            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);
      
            Transport.send(message);
      
            System.out.println("寄送email結束.");
            result = true;
      
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}