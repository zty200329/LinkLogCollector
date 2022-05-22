package io.github.zty200329.service;


import io.github.zty200329.domain.LogObject;

/**
 * @author zty200329
 * @version 1.0
 */
public interface LinkLogService {

    /**
     * 在引入jar包后，用户需要实现这个接口来定义日志的输出方式（输出到控制台、打印到文件。。随你自己操作即可）
     * 用于用户操作日志的接口，注解会将日志返回到这里，用户需要实现这个接口去定义怎么操作日志
     * @param logObject 对象
     * @return 随便返回 可以不返回
     */
    Object saveLog(LogObject logObject);
}
