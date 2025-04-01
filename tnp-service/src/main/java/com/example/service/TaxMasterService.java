package com.example.service;

import com.example.data.transactional.entities.TaxMaster;
import com.example.data.transactional.repo.TaxMasterRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class TaxMasterService {

    @Autowired private TaxMasterRepository taxMasterRepository;

    public TaxMaster getTaxMaster() {

        return taxMasterRepository.findByActive(true);
    }

    public List<TaxMaster> getTaxMaster1() {
        return taxMasterRepository.findAll();
    }
}
