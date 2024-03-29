package cn.xiaocai.limiter.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * springboot-limiter
 * TODO 限流注解--固定时间窗口期 period 内限流  maxLimited
 *      FCL : Fix Period Count Rate limiter
 * @author ZHANGXIAOCAI
 */
@Documented
@Retention( RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public  @interface RateLimitFCL {

    String failBack() default "" ;;

    /**
     * 最大限流数
     * @return
     */
    long maxLimited() default 3;

    /**
     * 限流窗口周期
     * @return
     */
    long period() default 10;

    /**
     *  限流窗口周期-单位 默认是s
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
