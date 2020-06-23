package br.com.yiatzz.coins.core.cache;

import br.com.yiatzz.coins.core.user.SimpleUser;
import br.com.yiatzz.coins.core.user.User;
import com.google.inject.Inject;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

import java.util.Optional;
import java.util.UUID;

import static br.com.yiatzz.coins.core.query.CustomQueryFactory.equalsIgnoreCase;
import static com.googlecode.cqengine.query.QueryFactory.equal;


public class CoinsCache {

    private final IndexedCollection<User> users;

    @Inject
    public CoinsCache() {
        users = new ConcurrentIndexedCollection<>();
    }

    public IndexedCollection<User> getUsers() {
        return users;
    }

    public User get(UUID uuid) {
        return users.retrieve(equal(SimpleUser.USER_ID, uuid)).uniqueResult();
    }

    public Optional<User> find(UUID uuid) {
        return Optional.ofNullable(users.retrieve(equal(SimpleUser.USER_ID, uuid)).uniqueResult());
    }

    public Optional<User> find(String name) {
        return Optional.ofNullable(users.retrieve(equalsIgnoreCase(SimpleUser.USER_NAME, name)).uniqueResult());
    }

    public void insert(User element) {
        users.add(element);
    }

    public void remove(User element) {
        users.remove(element);
    }
}
