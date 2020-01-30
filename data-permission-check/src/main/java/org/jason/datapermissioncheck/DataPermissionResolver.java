package org.jason.datapermissioncheck;

import javax.servlet.http.HttpServletRequest;

public interface DataPermissionResolver {

    boolean hasDataPermission(HttpServletRequest request, Object parameter);
}
