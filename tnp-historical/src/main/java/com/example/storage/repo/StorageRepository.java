package com.example.storage.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.storage.model.Stock;

import java.util.List;

@Repository
public interface StorageRepository extends MongoRepository<Stock, String> {

    /*List<Stock> findByNseSymbol(String firstName);*/
    Stock findByNseSymbol(String nseSymbol);

}
