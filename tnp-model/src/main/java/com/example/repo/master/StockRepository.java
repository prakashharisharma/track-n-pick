package com.example.repo.master;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.util.io.model.StockIO.IndiceType;

@Transactional
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	Stock findByIsinCode(String isinCode);
	
	Stock findByStockId(long stockId);
	
	Stock findFirstByNseSymbol(String nseSymbol);
	
	List<Stock> findByCompanyNameContainingIgnoreCase(String companyName);
	
	List<Stock> findByNseSymbolContainingIgnoreCase(String nseSymbol);
	
	List<Stock> findBySector(Sector sector);
	
	List<Stock> findByActive(boolean isActive);

	public List<Stock> findAll();
	
	public List<Stock> findByActiveAndPrimaryIndice(boolean isActive, IndiceType primaryIndice);
	public List<Stock> findByActiveAndPrimaryIndiceIn(boolean isActive, List<IndiceType> primaryIndiceList);
	
}
