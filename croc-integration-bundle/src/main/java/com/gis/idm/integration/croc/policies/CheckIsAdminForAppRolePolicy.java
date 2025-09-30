package com.gis.idm.integration.croc.policies;

import com.gis.idm.api.model.User;
import com.gis.idm.api.model.UsrAppRole;
import com.gis.idm.api.model.WorkflowRequest;
import com.gis.idm.api.model.enums.WorkflowRequestAction;
import com.gis.idm.api.policy.OptionalPolicyFunction;
import com.gis.idm.api.policy.PolicyResult;
import com.gis.idm.api.policy.exception.PolicyFunctionException;
import com.gis.idm.api.service.data.AppRoleService;
import com.gis.idm.api.service.data.UserService;
import com.gis.idm.api.service.data.UsrAppRoleService;
import com.gis.idm.api.service.data.WorkflowRequestService;
import com.gis.idm.api.util.GroupRequestUtil;
import com.gis.idm.integration.common.services.UserServiceWrapper;
import com.gis.idm.integration.croc.services.AdminIsAppRoleService;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourcePath;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.services.context.Context;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.gis.idm.api.policy.PolicyFunction.POLICY_ID;
import static org.osgi.framework.Constants.*;

@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                SERVICE_PID + "=" + CheckIsAdminForAppRolePolicy.PID,
                SERVICE_DESCRIPTION + "=" + CheckIsAdminForAppRolePolicy.DESCRIPTION,
                SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME,
                POLICY_ID + "=" + CheckIsAdminForAppRolePolicy.POLICY
        },
        service = {OptionalPolicyFunction.class}
)
public class CheckIsAdminForAppRolePolicy implements OptionalPolicyFunction {
    static final String DESCRIPTION = "GiS.IDM :: Policy for check whether users in request has isAdmin attribute set";
    static final String PID =  "com.gis.idm.integration.croc.policies.CheckIsAdminForAppRolePolicy";
    static final String POLICY = "CheckIsAdminForAppRolePolicy";

    static final Logger logger = LoggerFactory.getLogger(CheckIsAdminForAppRolePolicy.class);

    @Reference
    private WorkflowRequestService workflowRequestService;

    @Reference
    private AdminIsAppRoleService adminIsAppRoleService;
    @Reference
    private UserServiceWrapper userService;

    @Reference
    private UsrAppRoleService usrAppRoleService;


    /**
     * Разрешаем применение, если
     * 1. это новая заявка
     * 2. заявка про UsrAppRoles
     * 3. это заявка на выдачу прав
     *
     * @param context
     * @param resourceName
     * @param oldValue
     * @param newValue
     * @param property
     * @param config
     * @return
     */
    @Override
    public boolean shouldApply(Context context, ResourcePath resourceName, JsonValue oldValue, JsonValue newValue, String property, JsonValue config) {
        logger.info("shouldApply entered");
        logger.debug("with parameters: \n{}\n{}\n{}\n{}\n{}", resourceName, oldValue, newValue, property, config);
        WorkflowRequest workflowRequest =  workflowRequestService.build(newValue);
        return oldValue == null &&
                workflowRequest.getEntity().equals(UsrAppRole.LINKED_OBJECT_PATH) &&
                (workflowRequest.getAction().equals(WorkflowRequestAction.create)
                        || workflowRequest.getAction().equals(WorkflowRequestAction.batchCreate));
    }

    /**
     * Проверяем, что в каждой паре UsrAppRole если у IS, соответствующей AppRole, есть флаг isAdmin, то он должен быть и у User, иначе заявка отклоняется.
     *
     * @param context
     * @param resourcePath
     * @param jsonValue
     * @param s
     * @param jsonValue1
     * @return
     */
    @Override
    public PolicyResult apply(Context context, ResourcePath resourcePath, JsonValue jsonValue, String s, JsonValue jsonValue1) {
        logger.info("apply entered");
        logger.debug("with parameters:\n{}\n{}\n{}\n{}", resourcePath, jsonValue, s, jsonValue1);
        if (workflowRequestService.build(jsonValue)
                    .getBody()
                    .get("usrAppRoles")
                    .asList(item -> usrAppRoleService.build(item))
                    .stream().noneMatch(res -> {
                        boolean isUserAdmin = false;
                        try {
                            logger.info("findUserByUniqueAttributeValue {}", userService.findExistingByOuid(context, res.getUserId())
                                    .get().toJsonValue().toString());
                            isUserAdmin = userService.findExistingByOuid(context, res.getUserId())
                                    .get().toJsonValue().get("udfIsAdmin").asBoolean();
                            logger.info("userId: {}, isUserAdmin: {}", res.getUserId(), isUserAdmin);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Long appRoleId = res.getAppRoleId();
                        boolean isRoleAdmin = adminIsAppRoleService.isAppRoleFromAdminIs(context, appRoleId);
                        logger.info("roleId: {}, isRoleAdmin: {}", appRoleId, isRoleAdmin);
                        return isRoleAdmin && !isUserAdmin;
                    }))
                return PolicyResult.ok();
        return PolicyResult.failWithMessage("Административные роли не могут быт присвоены пользователю без включенного признака \"Администратор\"");
    }
}
