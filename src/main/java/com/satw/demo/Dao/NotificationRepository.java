package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository  extends JpaRepository<Notification, Long>{
	List<Notification> findAll();
	List<Notification> findById(int id);
	List<Notification> findByUserWalletAddress(String userWalletAddress);
}