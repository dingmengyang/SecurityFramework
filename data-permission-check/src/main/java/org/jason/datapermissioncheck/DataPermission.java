package org.jason.datapermissioncheck;

import java.lang.annotation.*;

/**
 * @Author jason
 * @Description 数据校验注解
 * @Date 2019/8/9
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    //需校验的参数名
    String parameterName();

    //对参数进行校验的resolver的beanName
    String resolverName();

    //为false需要判断注解方法的参数数组里是否有parameterName同名参数，有才获取对应resolver并调用hasDataPermission方法
    //为true则跳过判断，适合校验参数在封装类中时使用
    boolean forceCheck() default false;
}
