package com.satw.demo.Model;

import com.satw.demo.Controller.OrderController;

public interface State {
	public Msg update(Order order, OrderController orderController, User loginUser);
	public String getStateType();
}
