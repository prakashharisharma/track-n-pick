package com.example.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Stock;


@Transactional
@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

	Stock findByIsinCode(String isinCode);
	
	Stock findByNseSymbol(String nseSymbol);
	
	List<Stock> findByCompanyNameContainingIgnoreCase(String companyName);
	
	List<Stock> findByNseSymbolContainingIgnoreCase(String nseSymbol);
	
	List<Stock> findBySector(String sector);
	
	public List<Stock> findAll();
	
}