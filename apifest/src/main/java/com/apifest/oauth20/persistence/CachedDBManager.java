package com.apifest.oauth20.persistence;

import com.apifest.api.AccessToken;
import com.apifest.oauth20.*;
import com.apifest.oauth20.persistence.DBManager;
import com.apifest.oauth20.persistence.redis.RedisDBManager;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachedDBManager implements DBManager {

    // DbManaget is hardcoded to Redis for the time being. If it is passed to the constructor
    // This class can cache all types of storage.
    DBManager dbManager;
    LoadingCache<String, Optional<AccessToken>> tokensCache;

    public CachedDBManager(DBManager dbManager) {
        this.dbManager = dbManager;
        tokensCache = CacheBuilder.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build(new TokenCacheLoader());
    }

    private class TokenCacheLoader extends CacheLoader<String, Optional<AccessToken>> {
        @Override
        public Optional<AccessToken> load(String key) {
            return Optional.fromNullable(dbManager.findAccessToken(key));
        }
    }

    @Override
    public boolean validClient(String clientId, String clientSecret) {
        return dbManager.validClient(clientId, clientSecret);
    }

    @Override
    public void storeClientCredentials(ClientCredentials clientCreds) {
        dbManager.storeClientCredentials(clientCreds);
    }

    @Override
    public void storeAuthCode(AuthCode authCode) {
        dbManager.storeAuthCode(authCode);
    }

    @Override
    public void updateAuthCodeValidStatus(String authCode, boolean valid) {
        dbManager.updateAuthCodeValidStatus(authCode, valid);
    }

    @Override
    public void storeAccessToken(AccessToken accessToken) {
        tokensCache.invalidate(accessToken);
        dbManager.storeAccessToken(accessToken);
    }

    @Override
    public AccessToken findAccessTokenByRefreshToken(String refreshToken, String clientId) {
        return dbManager.findAccessTokenByRefreshToken(refreshToken, clientId);
    }

    @Override
    public void updateAccessTokenValidStatus(String accessToken, boolean valid) {
        tokensCache.invalidate(accessToken);
        dbManager.updateAccessTokenValidStatus(accessToken, valid);
    }

    @Override
    public AccessToken findAccessToken(String accessToken) {
        AccessToken loadedToken = null;
        try {
            loadedToken = tokensCache.get(accessToken).orNull();
        } catch (ExecutionException e) {

        }
        return loadedToken;
    }

    @Override
    public AuthCode findAuthCode(String authCode, String redirectUri) {
        return dbManager.findAuthCode(authCode, redirectUri);
    }

    @Override
    public ClientCredentials findClientCredentials(String clientId) {
        return dbManager.findClientCredentials(clientId);
    }

    @Override
    public boolean storeScope(Scope scope) {
        return dbManager.storeScope(scope);
    }

    @Override
    public List<Scope> getAllScopes() {
        return dbManager.getAllScopes();
    }

    @Override
    public Scope findScope(String scopeName) {
        return dbManager.findScope(scopeName);
    }

    @Override
    public boolean updateClientApp(String clientId, String scope, String description, Integer status, Map<String, String> applicationDetails, RateLimit rateLimit) {
        return dbManager.updateClientApp(clientId, scope, description, status, applicationDetails, rateLimit);
    }

    @Override
    public List<ApplicationInfo> getAllApplications() {
        return dbManager.getAllApplications();
    }

    @Override
    public boolean deleteScope(String scopeName) {
        return dbManager.deleteScope(scopeName);
    }

    @Override
    public List<AccessToken> getAccessTokenByUserIdAndClientApp(String userId, String clientId) {
        return dbManager.getAccessTokenByUserIdAndClientApp(userId, clientId);
    }

    @Override
    public void removeAccessToken(String accessToken) {
        tokensCache.invalidate(accessToken);
        dbManager.removeAccessToken(accessToken);
    }

    @Override
    public void removeUserTokens(String userId) {
        dbManager.removeUserTokens(userId);
    }
}
