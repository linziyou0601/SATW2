package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public class Placed implements State {
	private final String type = "Placed";
	public Msg update(Order order, OrderController orderController, User loginUser){
		Msg msg = new Msg();
		if(loginUser==null){
            msg = new Msg("Error", "Invalid operation.", "error");
        } else {
			if(order.getProductSeller().getId() == loginUser.getId()){
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
