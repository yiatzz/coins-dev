package br.com.yiatzz.coins.core.cache;

import br.com.yiatzz.coins.core.user.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

public class CoinsCache {

    private final Cache<String, User> CACHE;

    @Inject
    public CoinsCache() {
        CACHE = CacheBuilder.newBuilder().build();
    }

    public User select(String name) {
        return CACHE.getIfPresent(name);
    }

    public void insert(User element) {
        CACHE.put(element.getName(), element);
    }

    public void remove(String name) {
        CACHE.invalidate(name);
    }
}
