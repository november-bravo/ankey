package com.gis.idm.integration.croc;

import com.gis.idm.api.managed.HandlerResult;
import com.gis.idm.api.managed.ManagedObjectHandler;
import com.gis.idm.api.model.User;
import com.gis.idm.api.request.RequestService;
import com.gis.idm.api.service.data.UserService;
import com.gis.idm.settings.HandlerSetting;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.Request;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.forgerock.util.promise.PromiseImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                Constants.SERVICE_PID + "=" + DemoUserHandler.PID,
                Constants.SERVICE_DESCRIPTION + "=" + DemoUserHandler.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME,
                ManagedObjectHandler.PROPERTY_RESOURCE + "=" + User.MANAGED,
                ManagedObjectHandler.PROPERTY_ORDER + "=" + ManagedObjectHandler.DEFAULT_USER_LEVEL
        },
        service = {ManagedObjectHandler.class}
)
public class DemoUserHandler implements ManagedObjectHandler {
    private static final Logger logger = LoggerFactory.getLogger(DemoUserHandler.class);
    static final String PID = "DemoUserHandler";
    static final String DESCRIPTION = "GiS.IDM :: Demo User Handler Service";

    @Override
    public Promise<HandlerResult, ResourceException> onCreate(Context context, Request request, JsonValue object, Map<String, Object> args) {
        logger.info("Hello from DemoUser Handler!");
        return HandlerResult.ok().asPromise();
    }
}