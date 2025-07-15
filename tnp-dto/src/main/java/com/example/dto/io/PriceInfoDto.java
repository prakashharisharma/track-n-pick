package com.example.dto.io;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Builder
public class PriceInfoDto {
    double tickSize;
    double priceBand;
}
