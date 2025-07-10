package com.example.service;

import com.example.data.transactional.entities.Sector;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.SectorRepository;
import com.example.dto.io.SectorIO;
import com.example.service.impl.FundamentalResearchService;
import com.example.util.FormulaService;
import com.example.util.rules.RulesFundamental;
import java.util.List;
import java.util.Optional;
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
}
