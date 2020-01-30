package org.jason.datapermissioncheck;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.jason.datapermissioncheck.container.DataPermissionResolverContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataPermissionCheckInterceptor implements HandlerInterceptor {

    private DataPermissionResolverContainer dataPermissionResolverContainer;

    public DataPermissionCheckInterceptor(DataPermissionResolverContainer dataPermissionResolverContainer) {
        this.dataPermissionResolverContainer = dataPermissionResolverContainer;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (o instanceof HandlerMethod) {
            HandlerMethod ha = (HandlerMethod) o;
            DataPermissionResolver dataPermissionResolver = this.dataPermissionResolverContainer.getResolver(ha.getMethod(),ha.getBeanType());
            if (dataPermissionResolver != null) {
                DataPermission dataPermission = this.dataPermissionResolverContainer.getDataPermission(ha.getMethod(),ha.getBeanType());
                Object parameterValue = httpServletRequest.getParameter(dataPermission.parameterName());
                if(!dataPermissionResolver.hasDataPermission(httpServletRequest,parameterValue)) {
                    throw new DataPermissionException();
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
