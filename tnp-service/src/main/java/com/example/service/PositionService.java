package com.example.service;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.User;

public interface PositionService {

    public long calculate(User user, ResearchTechnical researchTechnical);
}
