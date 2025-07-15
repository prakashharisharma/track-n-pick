package com.example.service.utils;

import com.example.data.transactional.entities.ResearchTechnical;
import java.util.Optional;

public class SubStrategyHelper {

    public static Optional<ResearchTechnical.SubStrategy> resolveByName(String subStrategyName) {
        Optional<ResearchTechnical.SubStrategy> optional =
                ResearchTechnical.SubStrategy.fromName(subStrategyName);
        return optional.isPresent() ? Optional.of(optional.get()) : Optional.empty();
    }
}
