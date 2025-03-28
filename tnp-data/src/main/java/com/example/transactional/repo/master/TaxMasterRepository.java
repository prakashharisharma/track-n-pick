package com.example.transactional.repo.master;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.master.TaxMaster;


@Transactional
@Repository
public interface TaxMasterRepository extends JpaRepository<TaxMaster, Long>{

	TaxMaster findByActive(boolean active);
	List<TaxMaster> findAll();
}
