package cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import models.User;
import play.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author f.patin
 *         Date: 25/07/12
 *         Time: 19:35
 */
public class UserCache {
	private static final UserCache instance = new UserCache();

	private Cache<String, User> cache;

	private UserCache() {
		buildCache();
	}

	public static UserCache getInstance() {
		return instance;
	}

	public User getByUsername(String username) {
		User user = null;
		if (username != null) {
			try {
				user = cache.get(username);
			} catch (NullPointerException npe) {
				return null;
			} catch (ExecutionException e) {
				Logger.error(String.format("Error getUsername(%s)", username), e);
			}
		}
		return user;
	}

	public void invalidate(String username) {
		cache.invalidate(username);
	}

	public void invalidateAll() {
		cache.invalidateAll();
	}

	private void buildCache() {
		cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new CacheLoader<String, User>() {
					@Override
					public User load(final String username) throws Exception {
						return User.findByUserName(username);
					}
				});
	}

}
