package com.example.repo.master;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Broker;

@Transactional
@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long>{
	Broker findByBrokerId(long brokerId);
}
