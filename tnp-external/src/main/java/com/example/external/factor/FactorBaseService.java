package com.example.external.factor;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFactor;
import com.example.external.common.FactorProvider;
import java.io.IOException;
import java.net.MalformedURLException;

public interface FactorBaseService {

    FactorProvider getServiceProvider();

    StockFactor getFactor(Stock stock) throws MalformedURLException, IOException;
}
