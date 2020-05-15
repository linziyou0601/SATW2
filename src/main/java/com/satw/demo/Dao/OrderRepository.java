package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Order;
import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository  extends JpaRepository<Order, Long>{
	List<Order> findAll();
	List<Order> findById(int id);
	Order findFirstById(int id);
	List<Order> findByProduct(Product product);
	List<Order> findByBuyer(User buyer);
	@Query("select o from Order o join o.product p where o.buyer = :user or p.seller = :user")
	List<Order> findByOwner(@Param("user") User user);
}