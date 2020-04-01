package br.com.yiatzz.coins.core.cache;

import br.com.yiatzz.coins.core.user.User;
import com.google.inject.Inject;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoinsCache {

    private final Set<User> users;

    @Inject
    public CoinsCache() {
        users = ConcurrentHashMap.newKeySet();
    }

    public Set<User> getUsers() {
        return users;
    }

    public Optional<User> getByUniqueId(UUID uuid) {
        for (User user : users) {
            if (user.getUUID().equals(uuid)) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public Optional<User> getByName(String name) {
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public boolean addElement(User element) {
        return users.add(element);
    }

    public boolean removeElement(User element) {
        return users.remove(element);
    }

    public boolean contains(User element) {
        return users.contains(element);
    }
}
