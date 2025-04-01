package com.example.service;

import com.example.data.transactional.entities.Broker;
import com.example.data.transactional.repo.BrokerRepository;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class BrokerService {

    @Autowired private BrokerRepository brokerRepository;

    public Broker getBrokerById(long brokerId) {
        return brokerRepository.findByBrokerId(brokerId);
    }
}
