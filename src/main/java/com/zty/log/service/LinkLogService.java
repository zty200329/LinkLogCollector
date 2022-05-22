package com.zty.log.service;

import com.zty.log.domain.LogObject;

/**
 * @author zty200329
 * @version 1.0
 * @date 2022/5/22 11:56 上午
 * @描述：在引入jar包后，用户需要实现这个接口来定义日志的输出方式（输出到控制台、打印到文件。。随你自己操作即可）
 */
public interface LinkLogService {

    /**
     * 用于用户操作日志的接口，注解会将日志返回到这里，用户需要实现这个接口去定义怎么操作日志
     * @param logObject
     * @return
     */
    Object saveLog(LogObject logObject);
}
