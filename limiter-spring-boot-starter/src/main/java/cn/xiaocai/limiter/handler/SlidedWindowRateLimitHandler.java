package cn.xiaocai.limiter.handler;

import cn.xiaocai.limiter.annotation.RateLimitSWL;
import cn.xiaocai.limiter.core.IRateLimiter;
import cn.xiaocai.limiter.core.SlidedWindowRateLimiter;
import cn.xiaocai.limiter.service.RateLimiterContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @description: TODO 功能角色说明：
 * TODO 描述：
 * @author: 张小菜
 * @date: 2021/5/18 23:45
 * @version: v1.0
 */
@Slf4j
public class SlidedWindowRateLimitHandler extends AbstractRateLimitHandler implements LimitHandler {

    public SlidedWindowRateLimitHandler(){
    }

    public SlidedWindowRateLimitHandler(RateLimiterContext rateLimiterContext) {
        this.rateLimiterContext = rateLimiterContext;
    }

    private  RateLimiterContext rateLimiterContext;

    @Override
    public boolean handler(Method method){
        return defaultHandler(method);
    }


    @Override
    public IRateLimiter registry(Method method) {
        String methodName = method.toString();
        RateLimitSWL rateLimit = method.getAnnotation(RateLimitSWL.class);
        IRateLimiter rateLimiter = CACHE_LIMIT.get(methodName);
        if (Objects.isNull(rateLimiter)) {
            synchronized (this) {
                log.info("SlidingWindowRateLimiter will registry newInstance ... ");
                IRateLimiter swrLimiter = new SlidedWindowRateLimiter(rateLimit.blocks(), rateLimit.maxLimited(),
                        rateLimit.enableBlockAvg(), rateLimit.timeWindowPeriod(), rateLimit.timeUnit());
                CACHE_LIMIT.put(methodName, swrLimiter);
            }
        }
        return CACHE_LIMIT.get(methodName);
    }
}
