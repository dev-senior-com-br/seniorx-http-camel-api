package br.com.senior.seniorx.http.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.spi.PropertiesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeniorXHTTPRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeniorXHTTPRouteBuilder.class);

    protected final RouteBuilder builder;
    protected String baseUrl = "{{seniorx.baseurl}}";
    protected boolean anonymous = false;
    protected String domain;
    protected String service;
    protected String primitiveType;
    protected String primitive;

    public SeniorXHTTPRouteBuilder(RouteBuilder builder) {
        this.builder = builder;
    }

    public SeniorXHTTPRouteBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public SeniorXHTTPRouteBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public SeniorXHTTPRouteBuilder domain(String domain) {
        this.domain = domain;
        return this;
    }

    public SeniorXHTTPRouteBuilder service(String service) {
        this.service = service;
        return this;
    }

    public SeniorXHTTPRouteBuilder primitiveType(String primitiveType) {
        this.primitiveType = primitiveType;
        return this;
    }

    public SeniorXHTTPRouteBuilder primitive(String primitive) {
        this.primitive = primitive;
        return this;
    }

    public String getRoute(Exchange exchange) {
        Message message = exchange.getMessage();
        if (message.getHeader("route") != null) {
            return null;
        }
        PropertiesComponent properties = exchange.getContext().getPropertiesComponent();
        String route = resolve(properties, baseUrl);
        if (route == null) {
            return null;
        }
        if (route.endsWith("/")) {
            route = route.substring(0, route.length() - 1);
        }
        if (anonymous) {
            route += "/anonymous";
        }
        route += '/' + domain //
                + '/' + service //
                + '/' + primitiveType //
                + '/' + primitive;

        message.setHeader("route", route);
        message.setHeader("Content-Type", "application/json");

        LOGGER.info("Routing to {}", route);

        return route;
    }

    public ValueBuilder route() {
        return builder.method(this, "getRoute");
    }

    private String resolve(PropertiesComponent properties, String value) {
        if (value != null && value.startsWith("{{") && value.endsWith("}}")) {
            return properties.resolveProperty(value.substring(2, value.length() - 2)).orElse(null);
        }
        return value;
    }

}
