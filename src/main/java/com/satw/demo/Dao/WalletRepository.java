package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository  extends JpaRepository<Wallet, Long>{
	List<Wallet> findAll();
	List<Wallet> findById(int id);
}