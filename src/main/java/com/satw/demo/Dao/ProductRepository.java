package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Product;
import com.satw.demo.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>{
	List<Product> findAll();
	List<Product> findByDeleted(boolean deleted);
	List<Product> findByIdAndDeleted(int id, boolean deleted);
	Product findFirstByIdAndDeleted(int id, boolean deleted);
	List<Product> findBySellerAndDeleted(User seller, boolean deleted);
	Product findFirstBySellerAndDeleted(User seller, boolean deleted);
	
	@Query("select p from Product p where p.stockQty > 0 and p.deleted=false and (p.title like %:key% or p.description like %:key%) order by create_time desc, price asc")
    List<Product> findAllAvailable(@Param("key") String key);
}