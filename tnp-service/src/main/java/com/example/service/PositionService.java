package com.example.service;

import com.example.data.transactional.entities.ResearchTechnical;

public interface PositionService {

    public long calculate(Long userId, ResearchTechnical researchTechnical);
}
