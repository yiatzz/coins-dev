package br.com.yiatzz.coins.core.cache;

import br.com.yiatzz.coins.core.user.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

import java.util.UUID;

public class CoinsCache {

    private final Cache<UUID, User> users;

    @Inject
    public CoinsCache() {
        users = CacheBuilder.newBuilder().build();
    }

    public Cache<UUID, User> getUsers() {
        return users;
    }

    public User getIfPresent(UUID uuid) {
        return users.getIfPresent(uuid);
    }

    public User find(String name) {
        for (User value : users.asMap().values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }

        return null;
    }

    public void insert(User element) {
        users.put(element.getUUID(), element);
    }

    public void remove(UUID uuid) {
        users.invalidate(uuid);
    }
}
