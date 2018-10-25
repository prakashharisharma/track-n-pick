package com.example.chyl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.bl.CylhBaseService;
import com.example.common.ServiceProvider;

@Service
class CylhServiceFactory {

	@Autowired
    private List<CylhBaseService> chylServices;

    private static final Map<ServiceProvider, CylhBaseService> chylServiceCache = new HashMap<>();

    @PostConstruct
    void initMyServiceCache() {
        for(CylhBaseService chylService : chylServices) {
        	chylServiceCache.put(chylService.getServiceProvider(), chylService);
        }
    }

    static CylhBaseService getService(ServiceProvider serviceProvider) {
        CylhBaseService chylService = chylServiceCache.get(serviceProvider);
        if(chylService == null) throw new RuntimeException("Unknown service provider : " + serviceProvider);
        return chylService;
    }
}
