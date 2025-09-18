package com.gis.idm.integration;

import com.gis.idm.api.config.ConfigurationHelper;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openidm.core.ScriptRegistryServiceConstants;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.script.scope.Function;
import org.forgerock.script.scope.Parameter;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo transform function
 */
@Component(
        immediate = true,
        property = {
                Constants.SERVICE_PID + "=" + DemoTransform.PID,
                Constants.SERVICE_DESCRIPTION + "=" + DemoTransform.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME,
                ScriptRegistryServiceConstants.SCRIPT_SPACE + "=" + ScriptRegistryServiceConstants.PROP_TRANSFORM,
                ScriptRegistryServiceConstants.SCRIPT_NAME + "=" + DemoTransform.SCRIPT_NAME
        }
)
public class DemoTransform implements Function<String> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DemoTransform.class);
    static final String PID = ConfigurationHelper.DEFAULT_SERVICE_RDN_PREFIX + "demo.function";
    static final String DESCRIPTION = "GiS :: Demo Transform Function Service";
    static final String SCRIPT_NAME = "demoTransform";

    @Override
    public String call(Parameter parameter, Function<?> function, Object... objects) throws ResourceException, NoSuchMethodException {
        logger.info("Demo transform call");
        return "croc-integration";
    }
}
