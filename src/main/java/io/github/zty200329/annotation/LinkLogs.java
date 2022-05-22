package io.github.zty200329.annotation;

import java.lang.annotation.*;

/**
 * @author zty200329
 * @version 1.0
 * @date 2022/5/21 9:37 下午
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LinkLogs {
    LinkLog[] value();
}
