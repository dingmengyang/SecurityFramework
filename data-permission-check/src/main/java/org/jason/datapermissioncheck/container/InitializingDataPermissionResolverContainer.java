package org.jason.datapermissioncheck.container;


import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.jason.datapermissioncheck.DataPermission;
import org.jason.datapermissioncheck.DataPermissionResolver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author jason
 * @Description 容器类标准实现类：permissionResolverMap存储DataPermission注解parameterName与DataPermissionResolver的映射，
 * methodPermissionResolverMap存储处理方法与DataPermissionResolver的映射。
 * 两种思路：1.项目启动时容器类不做任何初始化，只传入applicationContext，getResolver时获取方法的DataPermission注解，applicationContext.getBean获取相应resolver并缓存
 *         2.项目启动时容器类即初始化所有DataPermission注解的method与对应的DataPermissionResolver的map，getResolver时从map获取即可
 *
 *         思路2的实现
 * @Date 2019/8/7
 **/
public class InitializingDataPermissionResolverContainer extends AbstractDataPermissionResolverContainer {
    
    private final Map<String, DataPermissionResolver> methodPermissionResolverMap = new HashMap<>(256);

    public InitializingDataPermissionResolverContainer(ApplicationContext applicationContext){
        super.setApplicationContext(applicationContext);
        initPermissionResolvers();
    }

    @Override
    public void addResolverInternal(String methodName, DataPermissionResolver resolver) {
        this.methodPermissionResolverMap.put(methodName,resolver);
    }

    @Override
    public void removeResolverInternal(String methodName) {
        this.methodPermissionResolverMap.remove(methodName);
    }

    @Override
    public DataPermissionResolver getResolver(Method method, Class<?> beanType) {
        String cacheKey=this.generateDefaultKey(method,beanType);
        return this.methodPermissionResolverMap.get(cacheKey);
    }

    @Override
    public void clear() {
        this.methodPermissionResolverMap.clear();
    }

    /**
     * @Author jason
     * @Description 初始化permissionResolverMap
     * @Date 2019/8/7
     **/
    private void initPermissionResolvers() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Looking for data permissions in application context: " + getApplicationContext());
        }

        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (!beanName.startsWith("scopedTarget.")) {
                Class<?> beanType = null;

                try {
                    beanType = getApplicationContext().getType(beanName);
                } catch (Throwable throwable) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Could not resolve target class for bean with name '" + beanName + "'", throwable);
                    }
                }
                //找出所有controller bean
                if (beanType != null && AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) && AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class)){
                    this.detectPermissionResolvers(beanType);
                }
            }
        }
    }

    private void detectPermissionResolvers(Class<?> handlerType) {
        final Class<?> userType = ClassUtils.getUserClass(handlerType);
        //遍历controller所有method，调用inspect返回DataPermissionResolver，并将Method和DataPermissionResolver封装成Map返回
        Map<Method, DataPermissionResolver> methods = MethodIntrospector.selectMethods(userType, new MethodIntrospector.MetadataLookup<DataPermissionResolver>() {
            @Override
            public DataPermissionResolver inspect(Method method) {
                try {
                    return InitializingDataPermissionResolverContainer.this.putDataPermissionResolverToMap(method, userType);
                } catch (Throwable var3) {
                    throw new IllegalStateException("Invalid permission resolver on handler class [" + userType.getName() + "]: " + method, var3);
                }
            }

        });
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(methods.size() + " request permission resolvers found on " + userType + ": " + methods);
        }

    }

    private DataPermissionResolver putDataPermissionResolverToMap(Method method, Class<?> userType) {
        DataPermission dataPermission = this.getDataPermission(method,userType);
        if (dataPermission == null) {
            return null;
        }
        if (!this.needCheckPermission(dataPermission,method,userType)){
            return null;
        }
        String cacheKey=this.generateDefaultKey(method,userType);
        DataPermissionResolver resolver = (DataPermissionResolver) getApplicationContext().getBean(dataPermission.resolverName());
        if (resolver==null&&getLogger().isDebugEnabled()){
            getLogger().debug("can not find DataPermissionResolver named "+dataPermission.resolverName()+" on " + userType + ": " + method.getName());
        }
        methodPermissionResolverMap.put(cacheKey,resolver);
        return resolver;
    }



}
