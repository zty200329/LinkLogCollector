package io.github.zty200329.annotation;

import java.lang.annotation.*;

/**
 * @author zty200329
 * @version 1.0
 */
// 注解的作用范围
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(LinkLogs.class)
public @interface LinkLog {
    /**
     * 业务唯一id
     * @return 返回uuId
     */
    String uuId();

    /**
     * 业务类型
     * @return 返回
     */
    String serviceType();

    /**
     * 需要传递的其他数据
     * @return 返回
     */
    String message();

    /**
     * 自定义的标签
     * @return 返回
     */
    String tag();
}
