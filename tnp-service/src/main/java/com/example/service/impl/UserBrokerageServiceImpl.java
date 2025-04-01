package com.example.service.impl;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.UserBrokerage;
import com.example.data.transactional.repo.UserBrokerageRepository;
import com.example.service.UserBrokerageService;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserBrokerageServiceImpl implements UserBrokerageService {

    @Autowired private UserBrokerageRepository userBrokerageRepository;

    @Override
    public double calculate(User user, double price, long quantity) {

        UserBrokerage userBrokerage =
                userBrokerageRepository.findByBrokerageIdUserAndActive(user, Boolean.TRUE);

        if (userBrokerage.getType() == UserBrokerage.Type.FIXED) {
            return calculateFixedCharges(userBrokerage, price, quantity);
        }

        return 0;
    }

    @Override
    public BigDecimal calculate(Long userId, double price, long quantity) {
        return BigDecimal.ZERO;
    }

    @Override
    public double getDpCharge(User user) {

        UserBrokerage userBrokerage =
                userBrokerageRepository.findByBrokerageIdUserAndActive(user, Boolean.TRUE);

        return userBrokerage.getDpCharge();
    }

    @Override
    public BigDecimal getDpCharge(Long userId) {
        return BigDecimal.ZERO;
    }

    private double calculateFixedCharges(UserBrokerage userBrokerage, double price, long quantity) {

        if (userBrokerage.getCalculation() == UserBrokerage.Calculation.PERCENTAGE) {
            return (price * quantity * userBrokerage.getPercentageCharge()) / 100;
        }

        return userBrokerage.getFlatCharge();
    }

    private double calculateMinimumCharges(
            UserBrokerage userBrokerage, double price, long quantity) {

        double percentage = (price * quantity * userBrokerage.getPercentageCharge()) / 100;

        double flat = userBrokerage.getFlatCharge();

        return (flat < percentage) ? flat : percentage;
    }
}
