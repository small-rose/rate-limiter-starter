package cn.xiaocai.limiter.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: springboot-limiter
 * @description
 * @function: 令牌桶 限流注解 TBL : Token Bucket Rate limiter
 * @author: zhangxiaocai
 * @create: 2021-05-21 22:17
 **/
@Documented
@Retention( RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RateLimitTBL {

    /**
     * 限流降级时执行的方法
     * @return
     */
    String failBack() default "" ;

    /**
     * 令牌桶里最大令牌数量
     * @return
     */
    long maxLimit() default 100;

    /**
     * 获取令牌-可以接受的等待（超时）时间
     * @return
     */
    long timeout() default 100 ;

    /**
     * 获取令牌-可以接受的等待（超时）时间 单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 每个执行周期 放入令牌桶 的令牌个数
     * @return
     */
    long createNumUnit() default 1;

    /**
     * 创建令牌的周期延迟
     * @return
     */
    long createDelay() default 1 ;

    /**
     * 创建令牌的周期
     * @return
     */
    long createPeriod() default 1;

    /**
     * 创建时的时间单位
     * @return
     */
    TimeUnit createTimeUnit() default TimeUnit.SECONDS;


}
