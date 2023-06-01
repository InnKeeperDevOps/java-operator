package run.innkeeper.api.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;
import run.innkeeper.api.dto.ws.WSSession;
import run.innkeeper.api.dto.ws.WSToken;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WSSessionStorage {
  LoadingCache<String, WSSession> sessions = CacheBuilder.newBuilder()
      .expireAfterWrite(12, TimeUnit.HOURS) // Set time to expire after write
      .build(
          new CacheLoader<>() {
            @Override
            public WSSession load(String key) {
              return null;
            }
          });
  LoadingCache<String, WSToken> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(5, TimeUnit.SECONDS) // Set time to expire after write
      .build(
          new CacheLoader<>() {
            @Override
            public WSToken load(String key) {
              return null;
            }
          });

  public LoadingCache<String, WSToken> getCache() {
    return cache;
  }

  public void setCache(LoadingCache<String, WSToken> cache) {
    this.cache = cache;
  }

  public LoadingCache<String, WSSession> getSessions() {
    return sessions;
  }

  public void setSessions(LoadingCache<String, WSSession> sessions) {
    this.sessions = sessions;
  }
}
