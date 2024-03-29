package com.example.repo.um;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface UserRepository extends JpaRepository<UserProfile, Long> {

	UserProfile findByUserId(long userId);
	UserProfile findByUserEmail(String userEmail);
	List<UserProfile> findByActive(boolean isActive);
	List<UserProfile> findByActiveAndSubscribePortfolio(boolean isActive, boolean subscribePortfolio);
	List<UserProfile> findByActiveAndSubscribeResearch(boolean isActive, boolean subscribeResearch);
	List<UserProfile> findByActiveAndSubscribeCurrentUndervalue(boolean isActive, boolean subscribeCurrentUndervalue);
	List<UserProfile> findAll();
	
	@Query(value = "select u from UserProfile u "
			+ "  JOIN  u.userDetails as ud"
			+ " WHERE u.userEmail =:username "
			)
	UserProfile findByUserName(@Param("username") String username);
	
}
