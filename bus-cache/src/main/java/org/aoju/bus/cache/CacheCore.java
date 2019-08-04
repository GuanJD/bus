package org.aoju.bus.cache;

import org.aoju.bus.cache.annotation.Cached;
import org.aoju.bus.cache.annotation.CachedGet;
import org.aoju.bus.cache.annotation.Invalid;
import org.aoju.bus.cache.entity.CacheHolder;
import org.aoju.bus.cache.entity.CacheMethod;
import org.aoju.bus.cache.entity.Expire;
import org.aoju.bus.cache.entity.Pair;
import org.aoju.bus.cache.invoker.BaseInvoker;
import org.aoju.bus.cache.reader.AbstractCacheReader;
import org.aoju.bus.cache.support.ArgNameGenerator;
import org.aoju.bus.cache.support.CacheInfoContainer;
import org.aoju.bus.cache.support.KeyGenerator;
import org.aoju.bus.cache.support.SpelCalculator;
import org.aoju.bus.logger.Logger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Singleton
public class CacheCore {

    @Inject
    private CacheConfig config;

    @Inject
    private CacheManager cacheManager;

    @Inject
    @Named("singleCacheReader")
    private AbstractCacheReader singleCacheReader;

    @Inject
    @Named("multiCacheReader")
    private AbstractCacheReader multiCacheReader;

    public static boolean isSwitchOn(CacheConfig config, Cached cached, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == CacheConfig.Switch.ON,
                cached.expire(), cached.condition(),
                method, args);
    }

    public static boolean isSwitchOn(CacheConfig config, Invalid invalid, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == CacheConfig.Switch.ON,
                Expire.FOREVER, invalid.condition(),
                method, args);
    }

    public static boolean isSwitchOn(CacheConfig config, CachedGet cachedGet, Method method, Object[] args) {
        return doIsSwitchOn(config.getCache() == CacheConfig.Switch.ON,
                Expire.FOREVER, cachedGet.condition(),
                method, args);
    }

    private static boolean doIsSwitchOn(boolean openStat,
                                        int expire,
                                        String condition, Method method, Object[] args) {
        if (!openStat) {
            return false;
        }

        if (expire == Expire.NO) {
            return false;
        }

        return (boolean) SpelCalculator.calcSpelValueWithContext(condition, ArgNameGenerator.getArgNames(method), args, true);
    }

    public Object read(CachedGet cachedGet, Method method, BaseInvoker baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(config, cachedGet, method, baseInvoker.getArgs())) {
            result = doReadWrite(method, baseInvoker, false);
        } else {
            result = baseInvoker.proceed();
        }

        return result;
    }

    public Object readWrite(Cached cached, Method method, BaseInvoker baseInvoker) throws Throwable {
        Object result;
        if (isSwitchOn(config, cached, method, baseInvoker.getArgs())) {
            result = doReadWrite(method, baseInvoker, true);
        } else {
            result = baseInvoker.proceed();
        }

        return result;
    }

    public void remove(Invalid invalid, Method method, Object[] args) {
        if (isSwitchOn(config, invalid, method, args)) {

            long start = System.currentTimeMillis();

            CacheHolder cacheHolder = CacheInfoContainer.getCacheInfo(method).getLeft();
            if (cacheHolder.isMulti()) {
                Map[] pair = KeyGenerator.generateMultiKey(cacheHolder, args);
                Set<String> keys = ((Map<String, Object>) pair[1]).keySet();
                cacheManager.remove(invalid.value(), keys.toArray(new String[keys.size()]));

                Logger.info("multi cache clear, keys: {}", keys);
            } else {
                String key = KeyGenerator.generateSingleKey(cacheHolder, args);
                cacheManager.remove(invalid.value(), key);

                Logger.info("single cache clear, key: {}", key);
            }

            Logger.debug("cache clear total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

    private Object doReadWrite(Method method, BaseInvoker baseInvoker, boolean needWrite) throws Throwable {
        long start = System.currentTimeMillis();

        Pair<CacheHolder, CacheMethod> pair = CacheInfoContainer.getCacheInfo(method);
        CacheHolder cacheHolder = pair.getLeft();
        CacheMethod cacheMethod = pair.getRight();

        Object result;
        if (cacheHolder.isMulti()) {
            result = multiCacheReader.read(cacheHolder, cacheMethod, baseInvoker, needWrite);
        } else {
            result = singleCacheReader.read(cacheHolder, cacheMethod, baseInvoker, needWrite);
        }

        Logger.debug("cache read total cost [{}] ms", (System.currentTimeMillis() - start));

        return result;
    }

    public void write() {
        // TODO on @CachedPut
    }

}