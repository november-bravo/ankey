package com.gis.idm.integration.croc.builders;

import com.gis.idm.api.annotation.ScheduledTaskTemplate;
import com.gis.idm.api.config.ConfigurationHelper;
import com.gis.idm.api.scheduler.TaskBuilderService;
import com.gis.idm.api.service.data.UserService;
import com.gis.idm.integration.common.services.UserServiceWrapper;
import com.gis.idm.integration.croc.DemoScheduleTask;
import com.gis.idm.integration.croc.scheduled.ClearHeadsWithFilterScheduledTask;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.services.context.Context;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ScheduledTaskTemplate("/scheduled/clear_heads_with_filter.json")
@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                Constants.SERVICE_PID + "=" + ClearHeadsWithFilterTaskBuilder.PID,
                Constants.SERVICE_DESCRIPTION + "=" + ClearHeadsWithFilterTaskBuilder.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME
        },
        service = {TaskBuilderService.class}
)
public class ClearHeadsWithFilterTaskBuilder implements TaskBuilderService<ClearHeadsWithFilterScheduledTask> {
    static final String PID = ConfigurationHelper.DEFAULT_SERVICE_RDN_PREFIX + "clearheads";
    static final String DESCRIPTION = "GiS.IDM :: Clear Heads Task Builder Service";
    private final Logger logger = LoggerFactory.getLogger(ClearHeadsWithFilterTaskBuilder.class);

    @Reference
    private UserServiceWrapper userServiceWrapper;

    @Override
    public ClearHeadsWithFilterScheduledTask createTask(Context context, Map<String, Object> scheduledContext) {
        logger.info("ClearHeadsWithFilterTaskBuilder createTask ENTERED");
        return new ClearHeadsWithFilterScheduledTask(context, scheduledContext)
                .setUserServiceWrapper(userServiceWrapper);
    }
}
