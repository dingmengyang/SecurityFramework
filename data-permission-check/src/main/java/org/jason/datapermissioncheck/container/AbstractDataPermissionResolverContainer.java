package org.jason.datapermissioncheck.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.jason.datapermissioncheck.DataPermission;
import org.jason.datapermissioncheck.DataPermissionResolver;

import java.lang.reflect.Method;


public abstract class AbstractDataPermissionResolverContainer implements DataPermissionResolverContainer, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(AbstractDataPermissionResolverContainer.class);

    private ApplicationContext applicationContext;

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    protected ApplicationContext getApplicationContext() {
        Assert.notNull(this.applicationContext, "DataPermissionResolverContainer have a null applicationContext");
        return this.applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "DataPermissionResolverContainer do not accept a null applicationContext");
        this.applicationContext = applicationContext;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    @Override
    public void addResolver(String name, DataPermissionResolver resolver) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("addResolver method do not accept a null or empty name");
        }
        if (resolver == null) {
            throw new IllegalArgumentException("addResolver method do not accept a null resolver");
        }
        this.addResolverInternal(name, resolver);
    }

    @Override
    public void removeResolver(String name) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("removeResolver method do not accept a null or empty key");
        }
        this.removeResolverInternal(name);
    }

    @Override
    public DataPermission getDataPermission(Method method, Class<?> beanType) {
        //注解是否在方法上，优先级更高
        DataPermission dataPermission = method.getAnnotation(DataPermission.class);
        if (dataPermission == null) {
            //注解是否在类上
            dataPermission = beanType.getAnnotation(DataPermission.class);
        }
        return dataPermission;
    }

    public String generateDefaultKey(Method method, Class<?> beanType) {
        StringBuilder sb = new StringBuilder(beanType.getName() + "_");
        sb.append(method.getName()).append("(");
        //为兼容重载方法，键名格式需带参数类型
        Class<?>[] types=method.getParameterTypes();
        for (int j = 0; j < types.length; j++) {
            sb.append(types[j].getName());
            if (j < (types.length - 1)) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    protected boolean needCheckPermission(DataPermission dataPermission, Method method, Class<?> beanType) {
        if (!(method.isAnnotationPresent(ResponseBody.class) || beanType.isAnnotationPresent(ResponseBody.class) || beanType.isAnnotationPresent(RestController.class))) {
            return false;
        }
        if (dataPermission.forceCheck()) {
            return true;
        }
        //处理方法参数是否含有dataPermission.parameterName()
        //由于java1.8才支持method.getParameters()方法获取方法参数名，所以采用spring的ParameterNameDiscoverer获取
        String[] parameterNames = this.parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames.length == 0) {
            return false;
        }
        boolean isParameterContained = false;
        for (String name : parameterNames) {
            if (dataPermission.parameterName().equals(name)) {
                isParameterContained = true;
                break;
            }
        }
        return isParameterContained;
    }

    protected abstract void addResolverInternal(String name, DataPermissionResolver resolver);

    protected abstract void removeResolverInternal(String name);
}
