package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.apache.ibatis.ognl.OgnlRuntime.setFieldValue;

/**
 * 自定义切面类
 * Aop
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    /**
     * 切面点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..))&& @annotation(com.sky.annotation.AutoFill)")
    public void  autoFillPointcut(){}

    /**
     * 通知
     */
    @Before("autoFillPointcut()")
    public  void  autoFill(JoinPoint joinPoint){
        log.info("开始拦截并填充");
        //填充time user
        //1. 获取当前被拦截方法的参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        //2. 获取方法上的AutoFill注解，判断是insert还是update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //3. 通过反射给实体类set公共字段（createTime、createUser、updateTime、updateUser）
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = BaseContext.getCurrentId(); //从ThreadLocal获取登录用户id

        if(operationType == OperationType.INSERT){
            //新增：4个字段都填充
            try {
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//            setFieldValue(entity,"setCreateTime",now);
//            setFieldValue(entity,"setCreateUser",currentUserId);
//            setFieldValue(entity,"setUpdateTime",now);
//            setFieldValue(entity,"setUpdateUser",currentUserId);
        }else if(operationType == OperationType.UPDATE){
            try {
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    }


}
