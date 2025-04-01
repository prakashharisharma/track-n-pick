package com.example.service;

import com.example.data.transactional.repo.UserRepository;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class UserService {

    @Autowired private UserRepository userRepository;
}
