package io.github.zty200329.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.github.zty200329.annotation.LinkLog;
import io.github.zty200329.domain.LogObject;
import io.github.zty200329.service.LinkLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.NamedThreadLocal;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author zty200329
 * @version 1.0
 */
@Component
@Aspect
@Slf4j
public class LinkLogAspect {
    @Autowired
    private LinkLogService linkLogService;

    /**
     * 创建Spring ExpressionParser解析表达式 spEL
     */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    private static final ThreadLocal<List<LogObject>> LOG_OBJECT_THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocal logObjectList");

    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Before(value = "@annotation(io.github.zty200329.annotation.LinkLog) || @annotation(io.github.zty200329.annotation.LinkLogs)")
    public void before(JoinPoint joinPoint){
        try {
            List<LogObject> logObjects = new ArrayList<>();
            LOG_OBJECT_THREAD_LOCAL.set(logObjects);
            Object[] arguments = joinPoint.getArgs();
            // 获取到方法对象
            Method method = getMethod(joinPoint);
            // 获取方法的注解，可以是多个
            LinkLog[] annotations = method.getAnnotationsByType(LinkLog.class);
            for(LinkLog annotation : annotations){
                LogObject logObject = new LogObject();
                logObjects.add(logObject);
                String uuIdSpEL = annotation.uuId();
                String messageSpEL = annotation.message();
                String uuId = uuIdSpEL;
                String message = messageSpEL;
                try {
                    String[] params = discoverer.getParameterNames(method);
                    EvaluationContext context = new StandardEvaluationContext();
                    if(params != null){
                        for (int len = 0; len <params.length ; len++) {
                            context.setVariable(params[len],arguments[len]);
                        }
                    }
                    // uuId 处理：直接传入字符串会抛出异常，写入默认传入的字符串
                    if (StringUtils.isNotBlank(uuIdSpEL)) {
                        //表达式放置
                        Expression bizIdExpression = parser.parseExpression(uuId);
                        //执行表达式，默认容器是spring本身的容器：ApplicationContext
                        uuId = bizIdExpression.getValue(context, String.class);
                    }
                    // massage 处理，写入默认传入的字符串
                    if (StringUtils.isNotBlank(messageSpEL)) {
                        Expression msgExpression = parser.parseExpression(messageSpEL);
                        Object msgObj = msgExpression.getValue(context, Object.class);
                        message = JSON.toJSONString(msgObj, SerializerFeature.WriteMapNullValue);
                    }
                } catch (Exception e){
                    log.error("LinkLogAspect do Before error",e);
                } finally {
                    logObject.setLogId(UUID.randomUUID().toString());
                    logObject.setSuccess(true);
                    logObject.setUuId(uuId);
                    logObject.setServiceType(annotation.serviceType());
                    logObject.setOperateDate(new Date());
                    logObject.setMassage(message);
                    logObject.setTag(annotation.tag());                }
            }
        } catch(Exception e){
            log.error("LinkLogAspect do Before error!",e);
        }
    }
    private Method getMethod(JoinPoint joinPoint){
        Method method = null;
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = joinPoint.getTarget();
        try {
            method = target.getClass().getMethod(methodSignature.getName(),methodSignature.getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("SystemLogAspect getMethod error",e);
        }
        return method;
    }
    @Around(value = "@annotation(io.github.zty200329.annotation.LinkLog) || @annotation(io.github.zty200329.annotation.LinkLogs)")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        Object result;
        try {
            result = pjp.proceed();
            // logDTO写入返回值信息 若方法抛出异常，则不会走入下方逻辑
            List<LogObject> logDTOList = LOG_OBJECT_THREAD_LOCAL.get();
            String returnStr = JSON.toJSONString(result);
            logDTOList.forEach(logDTO -> logDTO.setReturnStr(returnStr));
        }catch (Throwable throwable){
            // logDTO写入异常信息
            List<LogObject> logDTOList = LOG_OBJECT_THREAD_LOCAL.get();
            logDTOList.forEach(logDTO -> {
                logDTO.setSuccess(false);
                logDTO.setException(throwable.getMessage());
            });
            throw throwable;
        } finally {
            // logDTO发送至数据管道
            List<LogObject> logDTOList = LOG_OBJECT_THREAD_LOCAL.get();
            logDTOList.forEach(logDTO -> {
                try {
                    linkLogService.saveLog(logDTO);
                } catch (Throwable throwable) {
                    log.error("logRecord send message failure", throwable);
                }
            });
            LOG_OBJECT_THREAD_LOCAL.remove();
        }
        return result;
    }
}
