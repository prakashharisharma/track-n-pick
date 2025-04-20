package com.example.dto.mapper;

import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.io.StockTechnicalsDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class StockTechnicalsMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        // Optional: Define custom mappings if needed
        modelMapper.addMappings(
                new PropertyMap<StockTechnicals, StockTechnicalsDTO>() {
                    @Override
                    protected void configure() {
                        // map(source.getStock().getStockId(), destination.setStockId(null));  //
                        // Assuming Stock has stockId
                    }
                });
    }

    // Mapping from entity to DTO
    public static StockTechnicalsDTO toDTO(StockTechnicals stockTechnicals) {
        return modelMapper.map(stockTechnicals, StockTechnicalsDTO.class);
    }

    // Mapping from DTO to entity (if you need to save or update entity based on DTO)
    public static StockTechnicals toEntity(StockTechnicalsDTO stockTechnicalsDTO) {
        return modelMapper.map(stockTechnicalsDTO, StockTechnicals.class);
    }
}
