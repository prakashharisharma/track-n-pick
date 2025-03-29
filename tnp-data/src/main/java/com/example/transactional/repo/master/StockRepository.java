package com.example.transactional.repo.master;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.master.Sector;
import com.example.transactional.model.master.Stock;
import com.example.util.io.model.StockIO.IndiceType;

@Transactional
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

	Stock findByIsinCode(String isinCode);
	
	Stock findByStockId(long stockId);
	
	Stock findFirstByNseSymbol(String nseSymbol);

	@Query("SELECT s FROM Stock s WHERE LOWER(s.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.nseSymbol) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
	List<Stock> searchStock(@Param("searchTerm") String searchTerm);
	
	List<Stock> findByCompanyNameContainingIgnoreCase(String companyName);
	
	List<Stock> findByNseSymbolContainingIgnoreCase(String nseSymbol);
	
	List<Stock> findBySector(Sector sector);
	
	List<Stock> findByActive(boolean isActive);

	List<Stock> findByActivityCompleted(boolean isActivityCompleted);

	public List<Stock> findAll();
	
	public List<Stock> findByActiveAndPrimaryIndice(boolean isActive, IndiceType primaryIndice);
	public List<Stock> findByActiveAndPrimaryIndiceIn(boolean isActive, List<IndiceType> primaryIndiceList);
	
}
