package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>{
	List<Product> findAll();
	List<Product> findById(int id);
	List<Product> findBySeller(User seller);

	@Query("select p from Product p where p.stockQty > 0 and (p.title like %:key% or p.description like %:key%)")
    public List<Product> findAllAvailable(@Param("key") String key);
}