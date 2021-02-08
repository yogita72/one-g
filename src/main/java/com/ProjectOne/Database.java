package com.ProjectOne;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Database {

    private Set<String> authCodes = new HashSet<>();
    private Set<String> tokens = new HashSet<>();

    public void addAuthCode(String authCode) {
        authCodes.add(authCode);
    }

    public boolean isValidAuthCode(String authCode) {
        return authCodes.contains(authCode);
    }

    public void addToken(String token) {
        tokens.add(token);
    }

    public boolean isValidToken(String token) {
        return tokens.contains(token);
    }
}

