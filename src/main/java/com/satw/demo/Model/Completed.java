package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public class Completed implements State {
	private final String type = "Completed";
	public Msg update(Order order, OrderController orderController, User loginUser){
		return new Msg("Error", "Order has Completed", "error");
	}
	public String getType(){
		return type;
	};
}
