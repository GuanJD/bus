package org.aoju.bus.pager.plugin;

import org.aoju.bus.pager.PageException;
import org.aoju.bus.pager.cache.Cache;
import org.aoju.bus.pager.cache.CacheFactory;
import org.aoju.bus.pager.dialect.Dialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PageInterceptor implements Interceptor {

    protected Cache<String, MappedStatement> msCountMap = null;
    private volatile Dialect dialect;
    private String countSuffix = "_COUNT";
    private String default_dialect_class = "org.aoju.bus.pager.PageContext";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler resultHandler = (ResultHandler) args[3];
            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey;
            BoundSql boundSql;
            //由于逻辑关系，只会进入一次
            if (args.length == 4) {
                //4 个参数时
                boundSql = ms.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            } else {
                //6 个参数时
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }
            checkDialectExists();

            List resultList;
            //调用方法判断是否需要进行分页，如果不需要，直接返回结果
            if (!dialect.skip(ms, parameter, rowBounds)) {
                //判断是否需要进行 count 查询
                if (dialect.beforeCount(ms, parameter, rowBounds)) {
                    //查询总数
                    Long count = count(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                    //处理查询总数，返回 true 时继续分页查询，false 时直接返回
                    if (!dialect.afterCount(count, parameter, rowBounds)) {
                        //当查询总数为 0 时，直接返回空的结果
                        return dialect.afterPage(new ArrayList(), parameter, rowBounds);
                    }
                }
                resultList = CountExecutor.pageQuery(dialect, executor,
                        ms, parameter, rowBounds, resultHandler, boundSql, cacheKey);
            } else {
                //rowBounds用参数值，不使用分页插件处理时，仍然支持默认的内存分页
                resultList = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            return dialect.afterPage(resultList, parameter, rowBounds);
        } finally {
            dialect.afterAll();
        }
    }

    /**
     * Spring bean 方式配置时，如果没有配置属性就不会执行下面的 setProperties 方法，就不会初始化
     * <p>
     * 因此这里会出现 null 的情况 fixed #26
     */
    private void checkDialectExists() {
        if (dialect == null) {
            synchronized (default_dialect_class) {
                if (dialect == null) {
                    setProperties(new Properties());
                }
            }
        }
    }

    private Long count(Executor executor, MappedStatement ms, Object parameter,
                       RowBounds rowBounds, ResultHandler resultHandler,
                       BoundSql boundSql) throws SQLException {
        String countMsId = ms.getId() + countSuffix;
        Long count;
        //先判断是否存在手写的 count 查询
        MappedStatement countMs = CountExecutor.getExistedMappedStatement(ms.getConfiguration(), countMsId);
        if (countMs != null) {
            count = CountExecutor.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            countMs = msCountMap.get(countMsId);
            //自动创建
            if (countMs == null) {
                //根据当前的 ms 创建一个返回值为 Long 类型的 ms
                countMs = CountMappedStatement.newCountMappedStatement(ms, countMsId);
                msCountMap.put(countMsId, countMs);
            }
            count = CountExecutor.executeAutoCount(dialect, executor, countMs, parameter, boundSql, rowBounds, resultHandler);
        }
        return count;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        //缓存 count ms
        msCountMap = CacheFactory.createCache(properties.getProperty("msCountCache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (PageFromObject.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        try {
            Class<?> aClass = Class.forName(dialectClass);
            dialect = (Dialect) aClass.newInstance();
        } catch (Exception e) {
            throw new PageException(e);
        }
        dialect.setProperties(properties);

        String countSuffix = properties.getProperty("countSuffix");
        if (PageFromObject.isNotEmpty(countSuffix)) {
            this.countSuffix = countSuffix;
        }
    }

}