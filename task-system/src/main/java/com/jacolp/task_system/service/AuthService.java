package com.jacolp.task_system.service;

import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    String login(String username, String password);
    String register(String username, String password);
}
