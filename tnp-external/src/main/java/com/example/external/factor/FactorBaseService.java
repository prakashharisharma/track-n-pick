package com.example.external.factor;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFinancials;
import com.example.external.common.FactorProvider;
import java.io.IOException;
import java.net.MalformedURLException;

public interface FactorBaseService {

    FactorProvider getServiceProvider();

    StockFinancials getFactor(Stock stock) throws MalformedURLException, IOException;
}
