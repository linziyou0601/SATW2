package com.satw.demo.Model;

import com.satw.demo.Util.EmailUtil;
import com.satw.demo.Util.StringUtil;

import java.time.Instant;

public class Otp {

    public static enum ValidState {
        CORRECT, INCORRECT, EXPIRED, OVERTRIES
    } 

    private String otp;
    private long expiredTime;
    private int retryTimes;
    private String email;
    public Otp(String email){
        this.otp = StringUtil.generateOtp();
        this.expiredTime = Instant.now().getEpochSecond() + 300;
        this.retryTimes = 0;
        this.email = email;
    }

    public boolean send(){
        return EmailUtil.sendEmail(otp, email);
    }

    public ValidState verifyOtp(String otp){
        ValidState state = ValidState.CORRECT;
        retryTimes++;
    	if(Instant.now().getEpochSecond() > expiredTime) {
    		state = ValidState.EXPIRED;
    	} else if(!this.otp.equals(otp)) {
    		state = ValidState.INCORRECT;
    	}
    	if(state != ValidState.CORRECT && retryTimes >= 3 ) {
    		state = ValidState.OVERTRIES;
        }
        if(state == ValidState.EXPIRED){
            this.otp = StringUtil.generateOtp();
            this.expiredTime = Instant.now().getEpochSecond() + 300;
            send();
        }
    	return state;
    }
}