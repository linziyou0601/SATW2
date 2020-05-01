package com.satw.demo.Dao;

import java.util.List;

import com.satw.demo.Model.Profile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository  extends JpaRepository<Profile, Long>{
	List<Profile> findAll();
	List<Profile> findById(int id);
	List<Profile> findByName(String name);
	List<Profile> findByEmail(String email);
}