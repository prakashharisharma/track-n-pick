package com.example.service;

import com.example.service.utils.SupportResistanceZoneUtils;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupportResistanceZones {

    private SupportResistanceZoneUtils.Zone support;
    private SupportResistanceZoneUtils.Zone resistance;
}
