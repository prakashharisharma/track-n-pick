package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

@Slf4j
public class TradeSortHelper {

    public static Sort resolveSort(String sortBy, String direction) {
        boolean isAscending = direction.equalsIgnoreCase("asc");

        // Map API field names to entity field paths
        String sortField;
        switch (sortBy.toLowerCase()) {
            case "symbol":
                sortField = "stock.nseSymbol";
                break;
            case "type":
                sortField = "type";
                break;
            case "quantity":
                sortField = "quantity";
                break;
            case "price":
                sortField = "price";
                break;
            case "brokerage":
                sortField = "brokerage";
                break;
            default:
                sortField = "sessionDate"; // fallback sort
        }

        return isAscending ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
    }
}
