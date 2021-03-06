package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.StormpathAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.stormpath.StormpathProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link StormpathAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("stormpathAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class StormpathAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "stormpathPrincipalFactory")
    @Bean
    public PrincipalFactory stormpathPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationHandler stormpathAuthenticationHandler() {
        final StormpathProperties stormpath = casProperties.getAuthn().getStormpath();

        final StormpathAuthenticationHandler handler =
                new StormpathAuthenticationHandler(stormpath.getApiKey(), stormpath.getApplicationId(), stormpath.getSecretkey());

        handler.setPasswordEncoder(Beans.newPasswordEncoder(stormpath.getPasswordEncoder()));
        handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(stormpath.getPrincipalTransformation()));
        handler.setPrincipalFactory(stormpathPrincipalFactory());
        handler.setServicesManager(servicesManager);
        handler.setName(stormpath.getName());
        return handler;
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        final StormpathProperties stormpath = casProperties.getAuthn().getStormpath();
        if (StringUtils.isNotBlank(stormpath.getApiKey())
                && StringUtils.isNotBlank(stormpath.getSecretkey())) {
            this.authenticationHandlersResolvers.put(stormpathAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
}
