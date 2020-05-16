package com.satw.demo.Model;

import com.satw.demo.Normal.CreateNotifyLambda;
import com.satw.demo.Normal.Msg;

public class Canceled implements State {
	private final String type = "Canceled";
	public Msg update(Order order, CreateNotifyLambda<String, String, Integer, String, String, String> createNotifyLambda, User loginUser){
		return new Msg("Error", "Order has Canceled", "error");
	}
	public String getType(){
		return type;
	};
}
