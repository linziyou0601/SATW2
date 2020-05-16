package com.satw.demo.Model;

import com.satw.demo.Normal.CreateNotifyLambda;
import com.satw.demo.Normal.Msg;

public class Placed implements State {
	private final String type = "Placed";
	public Msg update(Order order, CreateNotifyLambda<String, String, Integer, String, String, String> createNotifyLambda, User loginUser){
		Msg msg = new Msg();
		if(loginUser==null){
            msg = new Msg("Error", "Invalid operation.", "error");
        } else {
			if(order.getProductSellerId() == loginUser.getId()){
				order.setState(new Shipped());  //狀態更新為已出貨
				msg = new Msg("Successful", "Order state has updated.", "success");
			} else {
				msg = new Msg("Error", "Invalid operation.", "error");
			}	
        }
		return msg;
	}
	public String getType(){
		return type;
	};
}
