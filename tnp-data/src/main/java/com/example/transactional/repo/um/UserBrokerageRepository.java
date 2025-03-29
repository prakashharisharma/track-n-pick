package com.example.transactional.repo.um;

import java.util.List;

import javax.transaction.Transactional;

import com.example.transactional.model.um.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.master.Broker;
import com.example.transactional.model.um.UserBrokerage;
import com.example.transactional.model.um.UserBrokerageKey;

@Transactional
@Repository
public interface UserBrokerageRepository extends JpaRepository<UserBrokerage, UserBrokerageKey>{
	
	List<UserBrokerage>  findByBrokerageIdUser(User user);
	
	UserBrokerage  findByBrokerageIdUserAndBrokerageIdBroker(User user, Broker broker);
	
	UserBrokerage  findByBrokerageIdUserAndActive(User user, boolean isActive);
	
}
