package com.gis.idm.integration.croc;

import com.gis.idm.api.scheduler.ExecutionException;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.task.InterruptableTaskExecution;
import org.forgerock.services.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.gis.idm.api.scheduler.TaskBuilderService.CONFIGURED_INVOKE_CONTEXT;
import static org.forgerock.json.JsonValue.json;


/**
 * Demo schedule task
 */
public class DemoScheduleTask extends InterruptableTaskExecution {
    private static final Logger logger = LoggerFactory.getLogger(DemoScheduleTask.class);
    private static final String ACTION_PARAM = "action";
    private final Map<String, Object> scheduledContext;

    public DemoScheduleTask(Context context, Map<String, Object> scheduledContext) {
        super(context);
        this.scheduledContext = scheduledContext;
    }

    @Override
    public void execute() throws ExecutionException {
        JsonValue params = json(scheduledContext).get(CONFIGURED_INVOKE_CONTEXT);
        JsonValue action = params.get(ACTION_PARAM);

        logger.info("Demo scheduled task started with action {}", action);
    }
}
