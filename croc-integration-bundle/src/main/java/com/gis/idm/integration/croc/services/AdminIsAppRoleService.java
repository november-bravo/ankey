package com.gis.idm.integration.croc.services;

import com.gis.idm.api.model.AppRole;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.util.promise.Promise;
import org.forgerock.services.context.Context;

import java.util.List;

public interface AdminIsAppRoleService {
    Promise<List<AppRole>, ResourceException> findAllIsAdminRoles(Context context);
}
