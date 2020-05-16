package com.satw.demo.Model;

import com.satw.demo.Normal.CreateNotifyLambda;
import com.satw.demo.Normal.Msg;

public class Shipped implements State {
	private final String type = "Shipped";
	public Msg update(Order order, CreateNotifyLambda<String, String, Integer, String, String, String> createNotifyLambda, User loginUser){
		order.setState(new Completed());  //狀態更新為已完成
		return new Msg("Successful", "Order state has updated.", "success");
	}
	public String getType(){
		return type;
	};
}
