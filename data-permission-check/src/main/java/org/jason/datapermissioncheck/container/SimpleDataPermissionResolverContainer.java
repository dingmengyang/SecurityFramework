package org.jason.datapermissioncheck.container;


import org.springframework.context.ApplicationContext;
import org.jason.datapermissioncheck.DataPermission;
import org.jason.datapermissioncheck.DataPermissionResolver;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author jason
 * @Description 容器类标准实现类：permissionResolverMap存储DataPermission注解parameterName与DataPermissionResolver的映射，
 * methodPermissionResolverCache存储处理方法与DataPermissionResolver的映射。
 * 两种思路：1.项目启动时容器类不做任何初始化，只传入applicationContext，getResolver时获取方法的DataPermission注解，applicationContext.getBean获取相应resolver并缓存
 *         2.项目启动时容器类即初始化所有DataPermission注解的method与对应的DataPermissionResolver的map，getResolver时从map获取即可
 *
 *         思路1的实现
 * @Date 2019/8/7
 **/
public class SimpleDataPermissionResolverContainer extends AbstractDataPermissionResolverContainer{

    private final Map<String, DataPermissionResolver> permissionResolverMap = new ConcurrentHashMap<>(12);
    private final Map<String, DataPermissionResolver> methodPermissionResolverCache = new ConcurrentHashMap<>(256);

    public SimpleDataPermissionResolverContainer(ApplicationContext applicationContext){
        setApplicationContext(applicationContext);
    }

    @Override
    public void addResolverInternal(String parameterNme, DataPermissionResolver resolver) {
        this.permissionResolverMap.put(parameterNme,resolver);
    }

    @Override
    public void removeResolverInternal(String parameterName) {
        this.permissionResolverMap.remove(parameterName);
    }

    @Override
    public DataPermissionResolver getResolver(Method method, Class<?> beanType) {
        String cacheKey=this.generateDefaultKey(method,beanType);
        DataPermissionResolver resolver=this.methodPermissionResolverCache.get(cacheKey);
        if (resolver==null) {
            DataPermission dataPermission=getDataPermission(method,beanType);
            if (dataPermission!=null&&needCheckPermission(dataPermission,method,beanType)) {
                resolver = this.permissionResolverMap.get(dataPermission.parameterName());
                if (resolver == null) {
                    resolver = (DataPermissionResolver) getApplicationContext().getBean(dataPermission.resolverName());
                    if (resolver==null){
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("can not find DataPermissionResolver named " + dataPermission.resolverName() + " on " + beanType.getName() + ": " + method.getName());
                        }
                        return null;
                    }
                    this.permissionResolverMap.put(dataPermission.parameterName(), resolver);
                }
            }else {
                return null;
            }
            this.methodPermissionResolverCache.put(cacheKey,resolver);
        }
        return resolver;
    }


    @Override
    public void clear() {
        this.permissionResolverMap.clear();
        this.methodPermissionResolverCache.clear();
    }

}
