package cn.xiaocai.limiter.service;

import cn.xiaocai.limiter.annotation.LimitTypeEnum;
import cn.xiaocai.limiter.core.IRateLimiter;
import cn.xiaocai.limiter.handler.FixPeriodCounterLimitHandler;
import cn.xiaocai.limiter.handler.LimitHandler;
import cn.xiaocai.limiter.handler.SlidedWindowRateLimitHandler;
import cn.xiaocai.limiter.handler.TokenBucketRateLimitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: TODO 功能角色说明：
 * TODO 描述：
 * @author: 张小菜
 * @date: 2021/5/20 20:21
 * @version: v1.0
 */
@Component
public class RateLimiterContext  {


    public static final Map<String, Class<? extends LimitHandler>> handlerMap = new HashMap<>();

    static {
        handlerMap.put(LimitTypeEnum.RateLimitSWL.name(), SlidedWindowRateLimitHandler.class);
        handlerMap.put(LimitTypeEnum.RateLimitFCL.name(), FixPeriodCounterLimitHandler.class);
        handlerMap.put(LimitTypeEnum.RateLimitTBL.name(), TokenBucketRateLimitHandler.class);
    }

    public  Map<String, Class<? extends LimitHandler>> getHandlerMap() {
        return handlerMap;
    }

    @Autowired
    private ApplicationContext applicationContext;

    public static  Map<Class<?>, Set<Method>> containsMap = new HashMap<>();
    /**
     *使用线程安全的ConcurrentHashMap存储所有实现Strategy接口的Bean
     *key:beanName
     *value：实现Strategy接口Bean
     */
    private final static Map<LimitTypeEnum, Class<IRateLimiter>> strategyMap = new ConcurrentHashMap<>();


    public static Map<LimitTypeEnum, Class<IRateLimiter>> getStrategyMap() {
        return strategyMap;
    }


    public IRateLimiter getLimiterStrategy(LimitTypeEnum type){
        Class<IRateLimiter> strategyClass = strategyMap.get(type);
        if(strategyClass==null){
            throw new IllegalArgumentException("没有对应的订单类型");
        }
        //从容器中获取对应的策略Bean
        return applicationContext.getBean(strategyClass);
    }
}
