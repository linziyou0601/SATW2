package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long>{
	List<User> findAll();
	User findFirstById(int id);
	List<User> findById(int id);
	List<User> findByAccount(String account);
	User findFirstByAccount(String account);
}