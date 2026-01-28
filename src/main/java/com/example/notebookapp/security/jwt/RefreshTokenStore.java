package com.example.notebookapp.security.jwt;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public String create(String email) {
        String token = UUID.randomUUID().toString();
        store.put(token, email);
        return token;
    }

    public String consume(String token) {
        return store.remove(token);
    }
}
