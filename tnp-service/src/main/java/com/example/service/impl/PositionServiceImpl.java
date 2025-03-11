package com.example.service.impl;

import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.um.UserProfile;
import com.example.service.FundsLedgerService;
import com.example.service.PositionService;
import com.example.service.RiskFactor;
import com.example.service.TradeProfitLedgerService;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    private FormulaService formulaService;
    @Autowired
    private FundsLedgerService fundsLedgerService;
    @Autowired
    private TradeProfitLedgerService tradeProfitLedgerService;

    @Override
    public long calculate(UserProfile userProfile, ResearchLedgerTechnical researchLedgerTechnical) {

        double investmentValue = fundsLedgerService.allTimeInvestment(userProfile);

        double netProfit = tradeProfitLedgerService.allTimeProfit(userProfile);

        double capital = investmentValue + netProfit;

        double riskFactor = this.getRiskFactor(researchLedgerTechnical);

        double risk = formulaService.calculateFraction(capital, riskFactor);

        double stopLoss = (researchLedgerTechnical.getResearchPrice()  - researchLedgerTechnical.getStopLoss());

        long positionSize = (long) (risk / stopLoss);

        log.info("user:{}, scrip:{}, research date:{}, strategy:{}, sub strategy:{}, risk:{}, positions:{}",
                userProfile.getUserId(), researchLedgerTechnical.getStock().getNseSymbol(), researchLedgerTechnical.getResearchDate(),
                researchLedgerTechnical.getStrategy(), researchLedgerTechnical.getSubStrategy(), risk, positionSize);

        return positionSize;
    }

    private double getRiskFactor(ResearchLedgerTechnical researchLedgerTechnical){
        if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.VOLUME){
            return RiskFactor.VOLUME_HV;
        }
        else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.PRICE){
            if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.SRMA) {
                return RiskFactor.PRICE_SRMA;
            }
            else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.SRTF) {
                return RiskFactor.PRICE_SRTF;
            }else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.RMAO) {
                return RiskFactor.PRICE_RMAO;
            }
        }
        else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.SWING){
            if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.RM) {
                return RiskFactor.SWING_RM;
            }
            else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.TEMA) {
                return RiskFactor.SWING_TEMA;
            }
        }

        return 0.0;
    }
}
