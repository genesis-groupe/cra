package cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import models.PayRate;
import play.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author leo
 *         Date: 30/12/12
 *         Time: 13:58
 */
public class PayRateCache {
	private static final PayRateCache instance = new PayRateCache();
	private Cache<String, PayRate> cache;
	private static final String KEY = "payRateCacheKey";

	private PayRateCache() {
		buildCache();
	}

	public static PayRateCache getInstance() {
		return instance;
	}

	public PayRate get(){
		try {
			return cache.get(KEY);
		} catch (ExecutionException e) {
			Logger.error("Get PayRate cache", e);
			return null;
		}
	}
	public void invalidate() {
		cache.invalidateAll();
	}

	private void buildCache() {
		cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new CacheLoader<String, PayRate>() {
					@Override
					public PayRate load(final String key) throws Exception {
						return PayRate.load();
					}
				});
	}
}
