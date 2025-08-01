package com.guijas1.checklistDell.Config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers((Context context) -> {
            context.setAllowCasualMultipartParsing(true);
        });

        factory.addConnectorCustomizers((Connector connector) -> {
            // Limite total por requisição (20MB)
            connector.setMaxPostSize(20 * 1024 * 1024);

            // Limite de arquivos por requisição
            connector.setProperty("maxParameterCount", "1000"); // segurança
            connector.setProperty("fileCountMax", "10"); // ← ESSENCIAL

            // Se estiver dando erro com maxSwallowSize
            connector.setProperty("maxSwallowSize", "-1");
        });
    }
}
