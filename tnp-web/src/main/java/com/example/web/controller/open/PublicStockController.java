package com.example.web.controller.open;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFactor;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.CrossOverLedgerService;
import com.example.service.StockPriceService;
import com.example.service.StockService;
import com.example.service.StockTechnicalsService;
import com.example.service.impl.FundamentalResearchService;
import com.example.util.FormulaService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/api/stocks")
public class PublicStockController {

    @Autowired private StockService stockService;

    @Autowired private FormulaService formulaService;

    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Autowired private CrossOverLedgerService crossOverLedgerService;

    @Autowired private BreakoutLedgerService breakoutLedgerService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @GetMapping(value = "/{stockId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStockFundamentals(@PathVariable long stockId) {

        Stock stock = stockService.getStockById(stockId);

        if (stock != null) {

            StockPrice stockPrice = stockPriceService.get(stockId, Timeframe.DAILY);

            StockFactor stockFactor = stock.getFactor();

            StockTechnicals stockTechnicals = stockTechnicalsService.get(stockId, Timeframe.DAILY);

            double pe = formulaService.calculatePe(stockPrice.getClose(), stockFactor.getEps());

            double pb =
                    formulaService.calculatePb(stockPrice.getClose(), stockFactor.getBookValue());

            String valuation = "NUETRAL";

            if (fundamentalResearchService.isUndervalued(stock)) {
                valuation = "UNDERVALUE";
            } else if (fundamentalResearchService.isOvervalued(stock)) {
                valuation = "OVERVALUED";
            } else {
                valuation = "NUETRAL";
            }

            double ema20 = stockTechnicals.getEma20();

            double ema50 = stockTechnicals.getEma50();

            double ema100 = stockTechnicals.getEma100();

            double ema200 = stockTechnicals.getEma200();

            return ResponseEntity.ok(null);
        } else {
            return ResponseEntity.ok("NOT FOUND");
        }
    }

    @GetMapping(
            value = "/searchstock",
            produces = {MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Stock>> searchStock1(@RequestParam String query) {

        List<Stock> stocksList = new ArrayList<>();

        List<Stock> stocksMasterList = stockService.activeStocks();

        return ResponseEntity.ok(stocksList);
    }
}
