package com.example.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Sector;
import com.example.model.master.Stock;
import com.example.repo.master.SectorRepository;
import com.example.util.FormulaService;
import com.example.util.rules.RulesFundamental;

@Transactional
@Service
public class SectorService {

	@Autowired
	private SectorRepository sectorRepository;
	
	@Autowired
	private FormulaService formulaService;
	
	@Autowired
	private RulesFundamental rules;
	
	@Autowired
	private RuleService ruleService;
	
	public Sector getSectorByName(String sectorName) {
		return sectorRepository.findBySectorNameContainingIgnoreCase(sectorName).get(0);
	}
	
	public Sector getOrAddSectorByName(String sectorName) {
		
		if(this.isExist(sectorName)) {
		
		return sectorRepository.findBySectorNameContainingIgnoreCase(sectorName).get(0);
		}else {
			return this.add(sectorName);
		}
	}
	
	public List<Sector> allSectors(){
		return sectorRepository.findAll();
	}
	
public Sector add(String sectorName) {
		
		Sector sector = new Sector(sectorName);
		
		return sectorRepository.save(sector);
	}
	
	public Sector add(String sectorName, double sectorPe, double sectorPb,double variationPe,double variationPb) {
		
		Sector sector = new Sector(sectorName, sectorPe, sectorPb, variationPe, variationPb);
		
		return sectorRepository.save(sector);
	}
	
	
	
	public Sector update(String sectorName, double newSectorPe, double newSectorPb,double newVariationPe,double newVariationPb) {
		
		Sector sector = this.getSectorByName(sectorName);
		
		sector.setSectorPe(newSectorPe);
		sector.setSectorPb(newSectorPb);
		sector.setVariationPe(newVariationPe);
		sector.setVariationPb(newVariationPb);
		
		return sectorRepository.save(sector);
	}
	
	public boolean isExist(String sectorName) {
		Sector sector = this.getSectorByName(sectorName);
		
		if(sector != null) {
			return true;
		}else {
			return false;
		}
	}
	
	public void updateSectorPEPB() {
		
		List<Sector> sectorList = this.allSectors();
		
	
		
		sectorList.forEach(sector -> {
			
			System.out.println(sector.getSectorName());
			
			Set<Stock> stocks = sector.getStocks();
			
			//List<Stock> activeStockList =  stocks.stream().filter(stock -> (stock.isActive() && stock.getStockPrice().getCurrentPrice() > rules.getPricegt() && stock.getStockPrice().getCurrentPrice() < rules.getPricelt() )).collect(Collectors.toList());
			List<Stock> activeStockList =  stocks.stream().filter(stock -> (stock.isActive() && ruleService.isPriceInRange(stock) )).collect(Collectors.toList());
			
			double avgCmp = activeStockList.stream().map(stock -> stock.getStockPrice()).mapToDouble(sp -> sp.getCurrentPrice()).average().orElse(0.00);
			
			activeStockList.forEach(System.out::println);
			
			System.out.println("AVERAGE_CMP : " + avgCmp);
			
			double avgEps = activeStockList.stream().map(stock -> stock.getStockFactor()).mapToDouble(sf -> sf.getEps()).average().orElse(0.00);
			
			System.out.println("AVERAGE_EPS : " + avgEps);
			
			double sectorPe = formulaService.calculatePe(avgCmp, avgEps);
			
			double variationPe = formulaService.calculatePercentage(sectorPe, 5);
			
			System.out.println("SECTOR_PE : " + sectorPe);
			
			double avgBookValue= activeStockList.stream().map(stock -> stock.getStockFactor()).mapToDouble(sf -> sf.getBookValue()).average().orElse(0.00);
			
			System.out.println("AVERAGE_BV : " + avgBookValue);
			
			double sectorPb = formulaService.calculatePb(avgCmp, avgBookValue);
			
			double variationPb= formulaService.calculatePercentage(sectorPb, 10);
			
			System.out.println("SECTOR_PB : " + sectorPb);
			
			sector.setSectorPe(sectorPe);
			sector.setSectorPb(sectorPb);
			sector.setVariationPe(variationPe);
			sector.setVariationPb(variationPb);
			sectorRepository.save(sector);
			
		});
	}
	
}
