package com.example.service.impl;

import com.example.enhanced.model.research.ResearchTechnical;
import com.example.model.um.User;
import com.example.service.FundsLedgerService;
import com.example.service.PositionService;
import com.example.service.RiskFactor;
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

    @Override
    public long calculate(User user, ResearchTechnical researchTechnical) {

        double investmentValue = fundsLedgerService.allTimeInvestment(user);

        double netProfit = 0.0;

        double capital = investmentValue + netProfit;

        double riskFactor = this.getRiskFactor(researchTechnical);

        double risk = formulaService.calculateFraction(capital, riskFactor);

        double stopLoss = (researchTechnical.getResearchPrice()  - researchTechnical.getStopLoss());

        long positionSize = (long) (risk / stopLoss);

        return positionSize;
    }

    private double getRiskFactor(ResearchTechnical researchTechnical){
        if(researchTechnical.getStrategy() == ResearchTechnical.Strategy.VOLUME){
            return RiskFactor.VOLUME_HV;
        }
        else if(researchTechnical.getStrategy() == ResearchTechnical.Strategy.PRICE){
            if(researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.SRMA) {
                return RiskFactor.PRICE_SRMA;
            }
            else if(researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.SRTF) {
                return RiskFactor.PRICE_SRTF;
            }else if(researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.RMAO) {
                return RiskFactor.PRICE_RMAO;
            }
        }
        else if(researchTechnical.getStrategy() == ResearchTechnical.Strategy.SWING){
            if(researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.RM) {
                return RiskFactor.SWING_RM;
            }
            else if(researchTechnical.getSubStrategy() == ResearchTechnical.SubStrategy.TEMA) {
                return RiskFactor.SWING_TEMA;
            }
        }

        return 0.0;
    }
}
