package com.gis.idm.integration.croc.services;

import com.gis.idm.api.model.AppRole;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.util.promise.Promise;
import org.forgerock.services.context.Context;

import java.util.List;

public interface AdminIsAppRoleService {
    /**
     * Найти все роли, соответствующие админским ИС
     *
     * @param context
     * @return
     */
    Promise<List<AppRole>, ResourceException> findAllIsAdminRoles(Context context);

    boolean isAppRoleFromAdminIs(Context context, Long appRoleId);

}
