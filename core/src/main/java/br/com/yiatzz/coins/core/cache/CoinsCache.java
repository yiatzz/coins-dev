package br.com.yiatzz.coins.core.cache;

import br.com.yiatzz.coins.core.user.SimpleUser;
import br.com.yiatzz.coins.core.user.User;
import com.google.inject.Inject;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.resultset.ResultSet;

import java.util.Optional;
import java.util.UUID;

import static br.com.yiatzz.coins.core.query.CustomQueryFactory.equalsIgnoreCase;
import static com.googlecode.cqengine.index.navigable.NavigableIndex.onAttribute;
import static com.googlecode.cqengine.query.QueryFactory.equal;

public class CoinsCache {

    private final IndexedCollection<User> users;

    @Inject
    public CoinsCache() {
        users = new ConcurrentIndexedCollection<>();
        users.addIndex(onAttribute(SimpleUser.USER_ID));
        users.addIndex(onAttribute(SimpleUser.USER_NAME));
    }

    public IndexedCollection<User> getUsers() {
        return users;
    }

    public User get(UUID uuid) {
        ResultSet<User> retrieve = users.retrieve(equal(SimpleUser.USER_ID, uuid));

        if (retrieve.isEmpty()) {
            return null;
        }

        return retrieve.uniqueResult();
    }

    public Optional<User> find(UUID uuid) {
        ResultSet<User> retrieve = users.retrieve(equal(SimpleUser.USER_ID, uuid));

        if (retrieve.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(retrieve.uniqueResult());
    }

    public Optional<User> find(String name) {
        ResultSet<User> retrieve = users.retrieve(equalsIgnoreCase(SimpleUser.USER_NAME, name));

        if (retrieve.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(retrieve.uniqueResult());
    }

    public void insert(User element) {
        users.add(element);
    }

    public void remove(User element) {
        users.remove(element);
    }
}
