package com.example.repo.stocks;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.stocks.UserPortfolioKey;
import com.example.model.type.SectorWiseValue;
import com.example.model.type.SectoralAllocation;
import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface PortfolioRepository extends JpaRepository<UserPortfolio, UserPortfolioKey> {

	List<UserPortfolio> findByPortfolioIdUser(UserProfile user);

	UserPortfolio findByPortfolioIdUserAndPortfolioIdStock(UserProfile user, Stock stock);

	List<UserPortfolio> findAll();

	@Query(value = "select s.sectorName, sum(up.averagePrice * up.quantity )  from UserPortfolio up "
			+ "  JOIN  up.portfolioId.stock as sm"
			+ "  JOIN  up.portfolioId.stock.sector as s"
			+ " WHERE up.portfolioId.user =:userId "
			+ " GROUP BY s.sectorName"
			)
	List<SectorWiseValue> findSectorWiseValue(@Param("userId") UserProfile userId);

	@Query(value = "select new com.example.model.type.SectoralAllocation(s.sectorName, sum(up.averagePrice * up.quantity ))  from UserPortfolio up "
			+ "  JOIN  up.portfolioId.stock as sm"
			+ "  JOIN  up.portfolioId.stock.sector as s"
			+ " WHERE up.portfolioId.user =:userId "
			+ " GROUP BY s.sectorName"
			)
	List<SectoralAllocation> findSectoralAllocation(@Param("userId") UserProfile userId);
	
	@Query(value = "select sum(sp.currentPrice * up.quantity )  from UserPortfolio up "
			+ "  JOIN  up.portfolioId.stock as sm"
			+ "  JOIN  up.portfolioId.stock.stockPrice as sp"
			+ " WHERE up.portfolioId.user =:userId "
			)
	Double getPortfolioCurrentValue(@Param("userId") UserProfile userId);
	
	@Query(value = "select sum(up.averagePrice * up.quantity )  from UserPortfolio up "
			+ "  JOIN  up.portfolioId.stock as sm"
			+ " WHERE up.portfolioId.user =:userId "
			)
	Double getPortfolioInvestedValue(@Param("userId") UserProfile userId);
	
}
