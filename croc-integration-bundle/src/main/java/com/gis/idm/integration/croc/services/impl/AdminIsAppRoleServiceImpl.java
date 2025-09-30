package com.gis.idm.integration.croc.services.impl;

import com.gis.idm.api.model.AppRole;
import com.gis.idm.api.model.InformationSystem;
import com.gis.idm.api.request.RequestService;
import com.gis.idm.api.service.data.InformationSystemService;
import com.gis.idm.integration.common.services.AppRoleServiceWrapper;
import com.gis.idm.integration.croc.services.AdminIsAppRoleService;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;

import static com.gis.idm.integration.common.services.Constants.VENDOR;
import static org.osgi.framework.Constants.*;

@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                SERVICE_PID + "=" + AdminIsAppRoleServiceImpl.PID,
                SERVICE_DESCRIPTION + "=" + AdminIsAppRoleServiceImpl.DESCRIPTION,
                SERVICE_VENDOR + "=" + VENDOR
        },
        service = {AdminIsAppRoleService.class}
)
public class AdminIsAppRoleServiceImpl implements AdminIsAppRoleService {
    static final String PID = "com.gis.idm.integration.croc.services.AdminIsAppRoleService";
    static final String DESCRIPTION = "Finds all roles for IS with isAdmin flag set";
    static final String UDF_IS_ADMIN = "udf_is_admin";

    private final static Logger logger = LoggerFactory.getLogger(AdminIsAppRoleServiceImpl.class);

    @Reference
    private InformationSystemService informationSystemService;
    @Reference
    private AppRoleServiceWrapper appRoleServiceWrapper;
    @Reference
    private RequestService requestService;

    @Override
    public Promise<List<AppRole>, ResourceException> findAllIsAdminRoles(Context context) {
        logger.info("findAllIsAdminRoles entered");
        Set<String> values = new HashSet<>();
        values.add("true");
        Set<Long> result = null;
        try {
            // запрос через api не работает с кастомными полями, поэтому через query
            String query = "select id from information_system where udf_is_admin = true";
            // query будет возвращать объекты типа InformationSystem
            var requestQuery = Requests.newQueryRequest(InformationSystem.getResourcePath()).setQueryExpression(query);
            logger.info("Query: {}", requestQuery.toString());
            //извлекаем коллекцию идентификаторов. замороченно, но вот так.
            //можно было прямо из JsonValue, но так нагляднее
            result = requestService.query(context, requestQuery)
                    .stream().map(resp -> informationSystemService.build(resp.getContent()).getOuid())
                    .collect(Collectors.toSet());
        } catch (Throwable e) {
            logger.error("findAllIsAdminRoles", e);
            throw new RuntimeException(e);
        }
        logger.info("ouids: {}", result.toString());
        return appRoleServiceWrapper.findAppRolesByInformationSystemsOuids(context, result);
    }

    @Override
    public boolean isAppRoleFromAdminIs(Context context, Long appRoleId) {
        logger.info("isAppRoleFromAdminIs entered");
        String query = "select is2.id " +
                "from information_system is2 join approle a " +
                "on a.is_id = is2.id " +
                "where a.id =  " + appRoleId.toString() + " " +
                "and is2.udf_is_admin = true";
        var requestQuery = Requests.newQueryRequest(InformationSystem.getResourcePath()).setQueryExpression(query);
        return !requestService.query(context, requestQuery).isEmpty();
    }
}
