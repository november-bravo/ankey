package com.gis.idm.integration.croc.builders;

import com.gis.idm.integration.croc.DemoScheduleTask;
import com.gis.idm.api.annotation.ScheduledTaskTemplate;
import com.gis.idm.api.config.ConfigurationHelper;
import com.gis.idm.api.scheduler.TaskBuilderService;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.services.context.Context;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import java.util.Map;


/**
 * Demo schedule task builder service
 */
@ScheduledTaskTemplate("/scheduled/demo-task-schema.json")
@Component(
        immediate = true,
        configurationPolicy = ConfigurationPolicy.IGNORE,
        property = {
                Constants.SERVICE_PID + "=" + DemoScheduleTaskBuilder.PID,
                Constants.SERVICE_DESCRIPTION + "=" + DemoScheduleTaskBuilder.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME
        },
        service = {TaskBuilderService.class}
)
public class DemoScheduleTaskBuilder implements TaskBuilderService<DemoScheduleTask> {
    static final String PID = ConfigurationHelper.DEFAULT_SERVICE_RDN_PREFIX + "demotask";
    static final String DESCRIPTION = "GiS.IDM :: Demo Scheduled Task Builder Service";

    @Override
    public DemoScheduleTask createTask(Context context, Map<String, Object> scheduledContext) {
        return new DemoScheduleTask(context, scheduledContext);
    }
}
