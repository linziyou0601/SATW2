package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public class Canceled implements State {
	private final String type = "Canceled";
	public Msg update(Order order, OrderController orderController, User loginUser){
		return new Msg("Error", "Order has Canceled", "error");
	}
	public String getType(){
		return type;
	};
}
