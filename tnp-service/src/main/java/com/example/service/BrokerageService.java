package com.example.service;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.entities.UserBrokerage;
import com.example.data.transactional.repo.UserBrokerageRepository;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class BrokerageService {

    @Autowired private UserBrokerageRepository userBrokerageRepository;

    public UserBrokerage getBrokerage(User user) {

        return userBrokerageRepository.findByIdUserAndActive(user, true);
    }
}
