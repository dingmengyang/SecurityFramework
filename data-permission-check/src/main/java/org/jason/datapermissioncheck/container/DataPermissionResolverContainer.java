package org.jason.datapermissioncheck.container;



import org.jason.datapermissioncheck.DataPermission;
import org.jason.datapermissioncheck.DataPermissionResolver;

import java.lang.reflect.Method;

/**
 * @Author jason
 * @Description DataPermissionResolver容器类
 * @Date 2019/8/5
 **/
public interface DataPermissionResolverContainer {

    void addResolver(String name, DataPermissionResolver resolver);

    void removeResolver(String name);

    DataPermissionResolver getResolver(Method method, Class<?> beanType);

    void clear();

    DataPermission getDataPermission(Method method, Class<?> beanType);
}
