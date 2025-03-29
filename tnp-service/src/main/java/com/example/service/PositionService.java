package com.example.service;

import com.example.transactional.model.research.ResearchTechnical;
import com.example.transactional.model.um.User;

public interface PositionService {

    public long calculate(User user, ResearchTechnical researchTechnical);
}
