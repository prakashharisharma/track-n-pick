package com.example.service;

import com.example.enhanced.model.research.ResearchTechnical;
import com.example.model.um.User;

public interface PositionService {

    public long calculate(User user, ResearchTechnical researchTechnical);
}
