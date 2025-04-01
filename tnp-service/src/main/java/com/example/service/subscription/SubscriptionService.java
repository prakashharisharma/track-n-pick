package com.example.service.subscription;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.types.SubscriptionType;
import java.util.List;

public interface SubscriptionService {

    public List<SubscriptionType> getActiveSubscriptionTypes(User user);
}
