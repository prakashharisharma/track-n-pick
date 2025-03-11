package com.example.service;

import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.um.UserProfile;

public interface PositionService {

    public long calculate(UserProfile userProfile, ResearchLedgerTechnical researchLedgerTechnical);
}
