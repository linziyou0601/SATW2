package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public class Ordered implements State {
	private final String type = "Ordered";
	public Msg update(Order order, OrderController orderController, User loginUser){
		orderController.requestSendUnshipNotify(order);//通知未出貨
		order.sendOrderDetail();        			   //發送明細
		order.setState(new Placed());   			   //狀態更新為已付款
		return new Msg("Successful", "Order state has updated.", "success");
	}
	public String getType(){
		return type;
	};
}
