package com.example.service;

import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.UserRepository;
import java.util.List;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class UserService {

    @Autowired private UserRepository userRepository;

    public User get(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public List<User> getAllDhanApiEnabledUsers() {
        return userRepository.findByEnabledAndDhanApiEnabled(true, true);
    }
}
