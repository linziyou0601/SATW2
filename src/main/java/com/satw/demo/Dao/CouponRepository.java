package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long>{
	List<Coupon> findAll();
	List<Coupon> findById(int id);
	List<Coupon> findByCode(String code);
	Coupon findFirstByCode(String code);
}