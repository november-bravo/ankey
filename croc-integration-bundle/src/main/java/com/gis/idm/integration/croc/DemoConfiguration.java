package com.gis.idm.integration.croc;

import com.gis.idm.api.config.ConfigurationHelper;
import org.forgerock.json.JsonValue;
import org.forgerock.openidm.config.enhanced.EnhancedConfig;
import org.forgerock.openidm.core.ServerConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo component for working with configuration
 *
 * see logging.properties configuration for this class
 */
@Component(
        immediate = true,
        configurationPid = DemoConfiguration.PID,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {
                Constants.SERVICE_PID + "=" + DemoConfiguration.PID,
                Constants.SERVICE_DESCRIPTION + "=" + DemoConfiguration.DESCRIPTION,
                Constants.SERVICE_VENDOR + "=" + ServerConstants.SERVER_VENDOR_NAME
        },
        service = {DemoConfigurationService.class}
)
public class DemoConfiguration implements DemoConfigurationService {
    static final String PID = ConfigurationHelper.DEFAULT_SERVICE_RDN_PREFIX + "demo";
    static final String DESCRIPTION = "GiS.IDM :: DemoConfiguration";
    private static final Logger logger = LoggerFactory.getLogger(DemoConfiguration.class);
    private static final String PROP_COMMENT = "comment";
    private JsonValue configuration;

    @Reference
    private EnhancedConfig configService;

    @Activate
    protected void activate(ComponentContext componentContext) {
        configuration = configService.getConfigurationAsJson(componentContext);
        logger.info("Demo config started with {}: {}", PROP_COMMENT, getComment());
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Demo config stopped.");
    }

    @Override
    public String getComment() {
        return configuration.get(PROP_COMMENT).asString();
    }
}
