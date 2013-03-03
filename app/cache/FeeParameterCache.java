package cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import models.FeesParameter;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author f.aptin
 */
public class FeeParameterCache {

	private static final FeeParameterCache instance = new FeeParameterCache();
	private Cache<F.Tuple<DateTime, DateTime>, FeesParameter> cache;

	private FeeParameterCache() {
		buildCache();
	}

	private void buildCache() {
		cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
				.maximumSize(100)
				.build(new CacheLoader<F.Tuple<DateTime, DateTime>, FeesParameter>() {
					@Override
					public FeesParameter load(F.Tuple<DateTime, DateTime> period) throws Exception {
						return FeesParameter.find(period._1, period._2);
					}
				});
	}

	public static FeesParameter get(final DateTime start, final DateTime end) {
		try {
			return instance.internalGet(start, end);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private FeesParameter internalGet(final DateTime start, final DateTime end) throws ExecutionException {
		return cache.get(F.Tuple(start, end));
	}

}
