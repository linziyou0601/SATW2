package com.satw.demo.Model;

import com.satw.demo.Normal.CreateNotifyLambda;
import com.satw.demo.Normal.Msg;

public class Ordered implements State {
	private final String type = "Ordered";
	public Msg update(Order order, CreateNotifyLambda<String, String, Integer, String, String, String> createNotifyLambda, User loginUser){
		//orderController.requestSendUnshipNotify(order);//通知未出貨
		order.notifyUnshipOrder(createNotifyLambda);
		order.sendOrderDetail();        			   //發送明細
		order.setState(new Placed());   			   //狀態更新為已付款
		return new Msg("Successful", "Order state has updated.", "success");
	}
	public String getType(){
		return type;
	};
}
