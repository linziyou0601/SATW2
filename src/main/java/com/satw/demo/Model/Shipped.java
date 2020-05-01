package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public class Shipped implements State {
	private final String stateType = "Shipped";
	public Msg update(Order order, OrderController orderController, User loginUser){
		order.setState(new Completed());  //狀態更新為已完成
		return new Msg("Successful", "Order state has updated.", "success");
	}
	public String getStateType(){
		return stateType;
	};
}
