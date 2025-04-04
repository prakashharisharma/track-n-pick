package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Sector;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.SectorRepository;
import com.example.dto.io.SectorIO;
import com.example.service.impl.FundamentalResearchService;
import com.example.util.FormulaService;
import com.example.util.rules.RulesFundamental;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
@Slf4j
public class SectorService {

    @Autowired private SectorRepository sectorRepository;

    @Autowired private FormulaService formulaService;

    @Autowired private RulesFundamental rules;

    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Autowired private StockService stockService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    public Optional<Sector> getByCode(String code) {
        return sectorRepository.findByCode(code);
    }

    public Sector getSectorByName(String sectorName) {

        System.out.println("Searching for sector : " + sectorName);

        List<Sector> sectorList = sectorRepository.findBySectorName(sectorName.trim());

        Sector sector = null;

        if (sectorList != null && sectorList.size() > 0) {
            sector = sectorList.get(0);
        }

        return sector;
    }

    public Sector getOrCreate(SectorIO sectorIO) {
        if (this.isExist(sectorIO.getCode())) {
            return sectorRepository.findByCode(sectorIO.getCode()).get();
        }

        Sector sector = new Sector(sectorIO.getSectorName().trim());
        sector.setCode(sectorIO.getCode().trim());
        return sectorRepository.save(sector);
    }

    public Sector getOrAddSectorByName(String sectorName) {

        if (this.isExistByName(sectorName)) {

            return getSectorByName(sectorName);

        } else {

            return this.add(sectorName);
        }
    }

    public List<Sector> allSectors() {
        return sectorRepository.findAll();
    }

    public Sector add(String sectorName) {

        Sector sector = new Sector(sectorName.toUpperCase().trim());

        return sectorRepository.save(sector);
    }

    public Sector add(
            String sectorName,
            double sectorPe,
            double sectorPb,
            double variationPe,
            double variationPb) {

        Sector sector =
                new Sector(
                        sectorName.toUpperCase().trim(),
                        sectorPe,
                        sectorPb,
                        variationPe,
                        variationPb);

        return sectorRepository.save(sector);
    }

    public Sector update(
            String sectorName,
            double newSectorPe,
            double newSectorPb,
            double newVariationPe,
            double newVariationPb) {

        Sector sector = this.getSectorByName(sectorName);

        sector.setSectorPe(newSectorPe);
        sector.setSectorPb(newSectorPb);
        sector.setVariationPe(newVariationPe);
        sector.setVariationPb(newVariationPb);

        return sectorRepository.save(sector);
    }

    public boolean isExistByName(String sectorName) {

        return this.getSectorByName(sectorName) != null ? true : false;
    }

    public boolean isExist(String sectorCode) {

        return this.getByCode(sectorCode).isPresent();
    }

    public void updateSectorPEPB() {

        log.info("Starting Update sector PE and PB");

        List<Sector> sectorList = this.allSectors();

        sectorList.forEach(
                sector -> {
                    double avgCmp = 0.00;
                    double avgEps = 0.00;
                    double sectorPe = 0.00;
                    double variationPe = 0.00;
                    double avgBookValue = 0.00;
                    double sectorPb = 0.00;
                    double variationPb = 0.00;
                    double sectorCurrentRatio = 0.00;

                    // System.out.println(sector.getSectorName());

                    // else{
                    try {
                        Set<Stock> stocks = sector.getStocks();

                        List<Stock> activeStockList =
                                stocks.stream()
                                        .filter(
                                                stock ->
                                                        (stock.isActive()
                                                                && stock.getFactor() != null))
                                        .collect(Collectors.toList());

                        avgCmp =
                                activeStockList.stream()
                                        .map(stock -> stockPriceService.get(stock, Timeframe.DAILY))
                                        .mapToDouble(sp -> sp.getClose())
                                        .average()
                                        .orElse(0.00);

                        // activeStockList.forEach(System.out::println);

                        // System.out.println("AVERAGE_CMP : " + avgCmp);

                        avgEps =
                                activeStockList.stream()
                                        .map(stock -> stock.getFactor())
                                        .mapToDouble(sf -> sf.getEps())
                                        .average()
                                        .orElse(0.00);

                        // System.out.println("AVERAGE_EPS : " + avgEps);

                        sectorPe = formulaService.calculatePe(avgCmp, avgEps);

                        variationPe = formulaService.calculateFraction(sectorPe, 5);

                        // System.out.println(sector.getSectorName() + " : PE : " + sectorPe);

                        avgBookValue =
                                activeStockList.stream()
                                        .map(stock -> stock.getFactor())
                                        .mapToDouble(sf -> sf.getBookValue())
                                        .average()
                                        .orElse(0.00);

                        // System.out.println("AVERAGE_BV : " + avgBookValue);

                        sectorPb = formulaService.calculatePb(avgCmp, avgBookValue);

                        variationPb = formulaService.calculateFraction(sectorPb, 10);

                        sectorCurrentRatio =
                                activeStockList.stream()
                                        .map(stock -> stock.getFactor())
                                        .mapToDouble(sf -> sf.getCurrentRatio())
                                        .average()
                                        .orElse(0.00);

                        // System.out.println(sector.getSectorName() + " : PB : " + sectorPb);

                        // }
                    } catch (Exception e) {
                        log.warn("An error occured ", e);
                    }
                    sector.setSectorPe(sectorPe);
                    sector.setSectorPb(sectorPb);
                    sector.setVariationPe(variationPe);
                    sector.setVariationPb(variationPb);
                    sector.setSectorCurrentRatio(sectorCurrentRatio);

                    sectorRepository.save(sector);
                });

        log.info("Completed Update sector PE and PB");
    }
}
