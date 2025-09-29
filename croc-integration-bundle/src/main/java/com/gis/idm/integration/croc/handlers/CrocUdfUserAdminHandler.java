package com.gis.idm.integration.croc.handlers;

import com.gis.idm.api.config.ConfigurationHelper;
import com.gis.idm.api.managed.HandlerResult;
import com.gis.idm.api.managed.ManagedObjectHandler;
import com.gis.idm.api.model.User;
import com.gis.idm.integration.common.entity.LoginGenerationConfig;
import com.gis.idm.integration.common.handlers.LoginGenerationHandler;
import com.gis.idm.integration.common.handlers.SwitchableHandler;
import com.gis.idm.integration.common.parsers.LoginGenerationConfigParser;
import com.gis.idm.integration.common.services.HandlerStatusService;
import com.gis.idm.integration.common.services.LoginGenerationService;
import com.gis.idm.settings.SystemSetting;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.Request;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.managed.model.UserImpl;
import org.forgerock.openidm.util.PromiseUtil;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.gis.idm.api.request.RequestService;
import com.gis.idm.api.service.data.UserService;
import com.gis.idm.settings.HandlerSetting;

import org.osgi.service.component.annotations.Reference;

import static com.gis.idm.api.managed.Event.onCreate;
import static com.gis.idm.api.managed.Event.onUpdate;


@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                Constants.SERVICE_PID + "=" + CrocUdfUserAdminHandler.PID,
                Constants.SERVICE_DESCRIPTION + "=" + CrocUdfUserAdminHandler.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME,
                ManagedObjectHandler.PROPERTY_RESOURCE + "=" + User.MANAGED,
                ManagedObjectHandler.PROPERTY_ORDER + "=" + ManagedObjectHandler.DEFAULT_USER_LEVEL,
                SystemSetting.NAME + "=" + CrocUdfUserAdminHandler.NAME
        },
        service = {ManagedObjectHandler.class, SystemSetting.class }
)
public class CrocUdfUserAdminHandler  extends SwitchableHandler implements ManagedObjectHandler, HandlerSetting {
    private static final Logger logger = LoggerFactory.getLogger(CrocUdfUserAdminHandler.class);
    static final String PID =  "com.gis.idm.integration.croc.handlers.CrocUdfUserAdminHandler";
    static final String DESCRIPTION = "GiS.IDM :: Croc User/Admin Handler Service";
    static final String NAME =  "CrocUdfUserAdminHandler";

    @Activate
    protected void activate(ComponentContext componentContext) {

        logger.info("Activating CrocUdfUserAdminHandler");
    }

    //todo: разобраться с конфигурациями. сделать префикс и указатель на поле параметрами

    @Reference
    private UserService userService;

    @Reference
    private LoginGenerationConfigParser handlerConfigParser;

    @Reference
    private LoginGenerationService loginGenerationService;

    @Reference
    private RequestService requestService;

    @Reference
    private HandlerStatusService handlerStatusService;


    @Override
    public String getHandlerId() {
        return PID;
    }

    @Override
    public HandlerStatusService getHandlerStatusService() {
        return handlerStatusService;
    }

//    @Override
//    public Promise<HandlerResult, ResourceException> onCreate(Context context, Request request, JsonValue object, Map<String, Object> args) {
    @Override
    public Promise<HandlerResult, ResourceException> onCreate(Context context, JsonValue object, Map<String, Object> args) {
        logger.info("onCreate entered");
        User user = userService.build(object);
        LoginGenerationConfig config = getLoginGeneratorConfig(onCreate.name());
        return generateAndAlterLoginForAdmin(context, user, config)
                .then(ignore -> HandlerResult.ok());
    }

 //   @Override
 //   public Promise<HandlerResult, ResourceException> onUpdate(Context context, Request request, JsonValue object, Map<String, Object> args, JsonValue oldObject, JsonValue newObject) {
    @Override
    public Promise<HandlerResult, ResourceException> onUpdate(Context context, JsonValue object, Map<String, Object> args, JsonValue oldObject, JsonValue newObject) {
        logger.info("onUpdate entered");
        read(context)
                .thenOnResultOrException(result -> {logger.info("Got config value: {}", result.get("config").get("justAnotherOne").asString());},
                        result -> {logger.error("ERROR: ", result);});
        User user = new UserImpl(newObject);
        user = updateUserName(user, user.getUserName(), getLoginGeneratorConfig(onUpdate.name()));

        newObject.put(UserService.ATTR_USER_NAME, user.getUserName());
        logger.info("onUpdate exited");
        return HandlerResult.ok().asPromise(); //super.onUpdate(context, object, args, oldObject, newObject);

    }

    private LoginGenerationConfig getLoginGeneratorConfig(String actionName) {
        logger.info("getLoginGeneratorConfig entered");

        return handlerConfigParser.getLoginGeneratorConfig(User.MANAGED_OBJECT, actionName);
    }

    private Promise<User, ResourceException> generateAndAlterLoginForAdmin(Context context, User user, LoginGenerationConfig config) {
        logger.info("generateAndAlterLoginForAdmin entered");

        return loginGenerationService.generateUniqueLogin(context, user, config)
                .then(login -> updateUserName(user, login, config));
    }

    private User updateUserName(User user, String login, LoginGenerationConfig config) {
        logger.info("updateUserName entered");

        if (login == null || login.isEmpty()) return user;
        if (login.startsWith("admin_"))
            login = login.substring("admin_".length());
        user.toJsonValue().put(UserService.ATTR_USER_NAME,
                user.toJsonValue().get("udfIsAdmin").asBoolean() ? "admin_" + login : login);
        logger.info("Extracting udfIsAdmin: {}", user.toJsonValue().get("udfIsAdmin"));
        return user;

    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getResource() {
        return User.MANAGED;
    }

    @Override
    public RequestService getRequestService() {
        return requestService;
    }

    @Override
    public String getConfigName() {
        return ConfigurationHelper.DEFAULT_SERVICE_RDN_PREFIX + NAME;
    }
}