package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>{
	@Query("select p from Product p where p.deleted=false")
	List<Product> findAll();
	@Query("select p from Product p where p.deleted=false and p.id=?1")
	List<Product> findById(int id);
	@Query("select p from Product p where p.deleted=false and p.seller=:seller")
	List<Product> findBySeller(@Param("seller") User seller);
	@Query("select p from Product p where p.stockQty > 0 and p.deleted=false and (p.title like %:key% or p.description like %:key%) order by create_time desc, price asc")
    public List<Product> findAllAvailable(@Param("key") String key);
}