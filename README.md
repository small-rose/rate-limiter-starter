# limiter-spring-boot-starter

## 服务限流与服务降级

1. 服务异常降级
2. 服务限流降级

### 1、异常服务降级。

使用注解@ExceptionFallBack 实现，具体的处理类是 ExceptionFallBackHandler

- 支持自定义fallback方法自定义，默认返回 {"code":500, "message", "系统异常，请稍后再试"}
- 支持针对特定的异常进行降级返回
- 支持排除特定的异常不进行降级处理


```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExceptionFallBack {
    /**
     *  发生异常时，降级执行的方法
     *  一般与api 方法与参数， 返回类型一致
     * @return
     */
    String fallback() default "" ;

    /**
     *  允许执行降级的异常 默认是 RuntimeException 和 Error
     *  写法类型 参考 spring事务注解遇到某些异常会回滚
     * @return
     */
    Class<? extends Throwable>[] fallbackFor() default {};

    /**
     *  允许执行降级的异常的名字 如 "NullPointException"
     * @return
     */
    String[] fallbackForClassName() default {};

    /**
     *  不允许执行降级的异常类 如 NullPointException.class
     * @return
     */
    Class<? extends Throwable>[] noFallbackFor() default {};

    /**
     *  不允许执行降级的异常的名字 如 "NullPointException"
     * @return
     */
    String[] noFallbackForClassName() default {};
}
```

### 2、异常限流降级。

**4种算法实现了API限流**

- 固定周期固定窗口算法
- 滑动窗口算法算法
- 令牌桶算法
- （令牌）漏斗算法

#### 2.1、固定周期固定窗口算法
    
  - 原理:使用计数器在限定的固定周期内累加访问次数，当达到设定的限流值时，触发限流策略。下一个周期开始时，计数器清零，重新计数。
  - 特点: 简单，容易实现，窗口期较大时，临界区间容易发生流量突击。此算法在单机还是分布式环境下实现都非常简单，使用redis的incr原子自增性和线程安全即可轻松实现。
  - 注意事项: 尽可能缩小窗口周期小
  - 使用场景: 常用于QPS限流和统计总访问量.

使用注解 @RateLimitFCL

使用示例：

```java
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

```    
    
#### 2.2、滑动窗口算法

 - 原理: 将时间周期分为N个小周期，分别记录每个小周期内访问次数，并且根据时间滑动删除过期的小周期内的统计数据。
 - 特点: 平滑流量。滑动窗口的格子划分的越多，那么滑动窗口的滚动就越平滑，限流的统计就会越精确。
 - 注意事项: 尽可能多的小格子来让请求均匀。
    
实现注解 @RateLimitSWL

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RateLimitSWL {
    /**
     *  到达限流时的降级方法名
     * @return
     */
    String failBack() default "" ;;

    /**
     * 滑动窗口的周期
     * @return
     */
    int timeWindowPeriod() default 60;

    /**
     * 滑动窗口的周期内的最大QPS流量
     * @return
     */
    long maxLimited() default 30L;

    /**
     * 滑动窗口可以分割多少个小窗口
     * @return
     */
    int blocks() default 10;

    /**
     * 滑动窗口就是为了平滑区间与流量，因此此值不建议修改
     * 如果改成false, 将变成  多个固定小窗口周期进行求和总限流
     * @return
     */
    boolean enableBlockAvg() default true;

    /**
     *
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```
    
#### 2.3、令牌桶算法
    
 - 原理: 程序以r（r=时间周期/限流值）的速度向令牌桶中增加令牌，直到令牌桶满，请求到达时向从令牌桶请求获取令牌，如获取到令牌则通过请求，否则触发限流策略
 - 特点: 允许一定的突击流量，请求空闲时，总令牌数为令牌桶中最大令牌数（最大突击流量限制）。
 - 注意事项: 保护自己的接口，防止高并发冲击。谷歌guava工具包中已有实现。
 ```$xslt
 <dependency>
     <groupId>com.google.guava</groupId>
     <artifactId>guava</artifactId>
     <version>${version}</version>
 </dependency>
 ```

实现注解 @RateLimitTBL
 
```java
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
```
  
- 漏斗算法
    - 原理: 也有称之为漏斗算法，访问请求到达时直接放入漏桶，如当前容量已达到上限（限流值），则进行丢弃（触发限流策略）。漏桶以固定的速率进行释放访问请求（即请求通过），直到漏桶为空。
    - 特点: 适合请求到达频率稳定、需要严格控制处理速率的场景
    - 注意事项: 当漏斗满时，后续请求速率基本与令牌（或水滴）流出速率一致，
    - 使用场景: 保护需要调用的其他接口
    

实现注解 @RateLimitLBL

```java

@Documented
@Retention( RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface RateLimitLBL {

    /**
     * 限流降级时执行的方法
     * @return
     */
    String failBack() default "" ;;

    /**
     * 令牌漏斗 最大令牌数量
     * @return
     */
    long maxLimit() default 100;

    /**
     * 存入令牌-可以接受的等待（超时）时间
     * @return
     */
    long timeout() default 100 ;

    /**
     * 获取令牌-可以接受的等待（超时）时间 单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 每时间单位 移出 N个令牌放
     * @return
     */
    long leakNum() default 1;

    /**
     * 移出 令牌的周期延迟
     * @return
     */
    long initialDelay() default 1 ;

    /**
     * 移出 令牌的 执行周期
     * @return
     */
    long period() default 1;

    /**
     * 移出 时的时间单位
     * @return
     */
    TimeUnit leakTimeUnit() default TimeUnit.SECONDS;
}
```

 
注解的简单使用示例：

```java
@RestController
public class LimitExampleController {


    @RateLimitSWL
    @RequestMapping("/test1")
    public String test(){
        return "SUCCESS API";
    }

    @RateLimitSWL
    @RequestMapping("/test2")
    public String test2(){
        return "SUCCESS API";
    }

    @RateLimitFCL(timeUnit = TimeUnit.SECONDS)
    @RequestMapping("/test3")
    public String test3(){
        return "SUCCESS API";
    }


    @RateLimitTBL(maxLimit=3 ,createPeriod = 5)
    @RequestMapping("/test4")
    public String test4(){
        return "SUCCESS api test4";
    }
}
```

更多用法参考 [https://github.com/small-rose/rate-limiter-starter/tree/main/limiter-spring-boot-example](https://github.com/small-rose/rate-limiter-starter/tree/main/limiter-spring-boot-example)
    