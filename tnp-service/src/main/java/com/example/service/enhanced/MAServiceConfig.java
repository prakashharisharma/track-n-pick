package com.example.service.enhanced;

import com.example.service.MovingAverageSupportResistanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MAServiceConfig {

    @Bean(name = "fiveDayMovingAverageService")
    public MovingAverageSupportResistanceService fiveDayMovingAverageService() {
        return new MovingAverageSupportResistanceServiceImpl(MovingAverageType.MA5);
    }

    @Bean(name = "twentyDayMovingAverageService")
    public MovingAverageSupportResistanceService twentyDayMovingAverageService() {
        return new MovingAverageSupportResistanceServiceImpl(MovingAverageType.MA20);
    }

    @Bean(name = "fiftyDayMovingAverageService")
    public MovingAverageSupportResistanceService fiftyDayMovingAverageService() {
        return new MovingAverageSupportResistanceServiceImpl(MovingAverageType.MA50);
    }

    @Bean(name = "hundredDayMovingAverageService")
    public MovingAverageSupportResistanceService hundredDayMovingAverageService() {
        return new MovingAverageSupportResistanceServiceImpl(MovingAverageType.MA100);
    }

    @Bean(name = "twoHundredDayMovingAverageService")
    public MovingAverageSupportResistanceService twoHundredDayMovingAverageService() {
        return new MovingAverageSupportResistanceServiceImpl(MovingAverageType.MA200);
    }
}
