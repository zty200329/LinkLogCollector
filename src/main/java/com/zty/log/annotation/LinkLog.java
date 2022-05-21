package com.zty.log.annotation;

import java.lang.annotation.*;

/**
 * @author zty200329
 * @version 1.0
 * @date 2022/5/21 8:50 下午
 */
// 注解的作用范围
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(LinkLogs.class)
public @interface LinkLog {
    /**
     * 业务唯一id
     * @return
     */
    String uuid();

    /**
     * 业务类型
     * @return
     */
    String serviceType();

    /**
     * 需要传递的其他数据
     * @return
     */
    String message();

    /**
     * 自定义的标签
     * @return
     */
    String tag();
}
