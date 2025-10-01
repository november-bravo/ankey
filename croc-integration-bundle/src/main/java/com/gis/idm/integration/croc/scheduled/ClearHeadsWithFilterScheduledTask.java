package com.gis.idm.integration.croc.scheduled;

import com.gis.idm.api.model.User;
import com.gis.idm.api.scheduler.ExecutionException;
import com.gis.idm.api.service.data.UserService;
import com.gis.idm.integration.common.services.UserServiceWrapper;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.PatchOperation;
import org.forgerock.json.resource.QueryFilters;
import org.forgerock.openidm.task.InterruptableTaskExecution;
import org.forgerock.services.context.Context;
import org.forgerock.util.query.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gis.idm.api.scheduler.TaskBuilderService.CONFIGURED_INVOKE_CONTEXT;
import static org.forgerock.json.JsonValue.json;


/**
 * Clear Heads schedule task
 */
public class ClearHeadsWithFilterScheduledTask extends InterruptableTaskExecution {
    private static final Logger logger = LoggerFactory.getLogger(ClearHeadsWithFilterScheduledTask.class);
    private final Map<String, Object> scheduledContext;

    public ClearHeadsWithFilterScheduledTask(Context context, Map<String, Object> scheduledContext) {
        super(context);
        this.scheduledContext = scheduledContext;
    }
    private UserServiceWrapper userServiceWrapper;


    public ClearHeadsWithFilterScheduledTask setUserServiceWrapper(UserServiceWrapper userServiceWrapper) {
        this.userServiceWrapper = userServiceWrapper;
        return this;
    }

    @Override
    public void execute() throws ExecutionException {
        logger.info("Clear heads with filter");
        JsonValue params = json(scheduledContext).get(CONFIGURED_INVOKE_CONTEXT);
        logger.debug("Clear Heads task scheduled task started with context:\n{}", json(scheduledContext));
        QueryFilter<JsonPointer> paramFilter = QueryFilters.parse(params.get("userFilter").asString());
        String FILTER_HAS_HEAD = "managerId pr";
        QueryFilter<JsonPointer> hasHeadFilter = QueryFilters.parse(FILTER_HAS_HEAD);
        if (isInterrupted()) return;
        try {
            userServiceWrapper.getUsersByFilter(getContext(), QueryFilter.and(paramFilter, hasHeadFilter))
                    .get()
                    .forEach(user -> userServiceWrapper.patch(getContext(), user.getId(), List.of(PatchOperation.operation(PatchOperation.OPERATION_REMOVE, "managerId", user.getManager()))));
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true){
            if (isInterrupted()) {
                logger.info("Clear heads task scheduled task stopped");
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


/*
пример scheduledContext
{ "scheduler.invokeService": "org.forgerock.openidm.clearheads",
"scheduler.config-name": "scheduler",
"scheduler.invokeContext": { "userFilter": "firstName eq 'James'" },
"scheduler.job-name": "Clear heads",
"schedule.config": "{ \"enabled\": false, \"persisted\": true, \"misfirePolicy\": \"doNothing\", \"schedule\": \"0 0 0/1 * * ?\", \"type\": null, \"invokeService\": \"org.forgerock.openidm.clearheads\", \"executeOn\": null, \"invokeContext\": { \"userFilter\": \"firstName eq 'James'\" }, \"invokeLogLevel\": \"info\", \"timeZone\": null, \"startDate\": null, \"endDate\": null, \"concurrentExecution\": false, \"lastStartTime\": \"2025-10-01T10:27:50.530+0000\", \"lastEndTime\": \"2025-10-01T10:27:50.507+0000\", \"lastStatus\": \"success\", \"lastErrorDescription\": null, \"description\": null }",
"scheduler.invoker-name": "Scheduled Clear heads-Wed Oct 01 10:30:03 UTC 2025",
"scheduler.invokeLogLevel": "info",
"scheduler.scheduled-fire-time": Wed Oct 01 10:30:03 UTC 2025,
"scheduler.actual-fire-time": Wed Oct 01 10:30:03 UTC 2025,
"scheduler.next-fire-time": null }

*/