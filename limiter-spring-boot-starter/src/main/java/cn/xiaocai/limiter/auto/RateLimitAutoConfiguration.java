package cn.xiaocai.limiter.auto;


import cn.xiaocai.limiter.spring.SpringFallBackAspect;
import cn.xiaocai.limiter.spring.SpringRateLimitAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @program: springboot-demo
 * @description
 * @function:
 * @author: zzy
 * @create: 2021-05-21 13:58
 **/
@Slf4j
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RateLimitAutoConfiguration {

    /**
     * rate limit  aspect spring boot rate limit  aspect.
     *
     * @return the spring boot rate limit aspect
     */
    @ConditionalOnMissingBean(SpringRateLimitAspect.class)
    @Bean
    public SpringRateLimitAspect springRateLimitAspect() {
        log.info("Initializing Spring RateLimitAspect ");
        return new SpringRateLimitAspect();
    }

    /**
     * FallBack  aspect spring boot fall back  aspect.
     *
     * @return the spring boot fall back aspect
     */
    @ConditionalOnMissingBean(SpringFallBackAspect.class)
    @Bean
    public SpringFallBackAspect springFallBackAspect() {
        log.info("Initializing Spring FallBackAspect ");
        return new SpringFallBackAspect();
    }
}
