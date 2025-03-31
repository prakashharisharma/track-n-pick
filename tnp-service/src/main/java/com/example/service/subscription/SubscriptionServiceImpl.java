package com.example.service.subscription;

import com.example.data.transactional.types.SubscriptionType;
import com.example.data.transactional.entities.User;

import com.example.data.transactional.repo.UserSubscriptionDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService{

    private final UserSubscriptionDetailRepository userSubscriptionDetailRepository;

    @Override
    public List<SubscriptionType> getActiveSubscriptionTypes(User user) {
        List<String> subscriptionCodes = userSubscriptionDetailRepository.findActiveSubscriptionTypesByUserId(user.getId());

        // Convert string results to SubscriptionType Enum
        return subscriptionCodes.stream()
                .map(SubscriptionType::valueOf)
                .toList();
    }

}
