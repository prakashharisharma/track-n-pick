package com.example.transactional.repo.subscription;

import com.example.transactional.model.subscription.entity.UserSubscriptionDetail;
import com.example.transactional.model.subscription.entity.UserSubscriptionOrder;
import com.example.transactional.model.subscription.type.SubscriptionType;
import com.example.transactional.model.um.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionDetailRepository extends JpaRepository<UserSubscriptionDetail, Long> {

    @Query(value = """
        SELECT DISTINCT sp.code 
        FROM user_subscription_details usd
        JOIN user_subscription_orders uso ON usd.order_id = uso.id
        JOIN subscription_plans sp ON usd.subscription_plan_id = sp.id
        WHERE uso.user_id = :userId
    """, nativeQuery = true)
    List<String> findSubscriptionTypesByUserId(@Param("userId") Long userId);
}
