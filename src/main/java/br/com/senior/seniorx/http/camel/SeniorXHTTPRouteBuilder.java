package br.com.senior.seniorx.http.camel;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeniorXHTTPRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeniorXHTTPRouteBuilder.class);

    protected final RouteBuilder builder;
    protected String url = "{{seniorx.url}}";
    protected boolean anonymous = false;
    protected boolean insecure = true;
    protected String method;
    protected String domain;
    protected String service;
    protected PrimitiveType primitiveType;
    protected String primitive;

    public SeniorXHTTPRouteBuilder(RouteBuilder builder) {
        this.builder = builder;
    }

    public SeniorXHTTPRouteBuilder method(String method) {
        this.method = method;
        return this;
    }

    public SeniorXHTTPRouteBuilder url(String url) {
        this.url = url;
        return this;
    }

    public SeniorXHTTPRouteBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public SeniorXHTTPRouteBuilder insecure(boolean insecure) {
        this.insecure = insecure;
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

    public SeniorXHTTPRouteBuilder primitiveType(PrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
        return this;
    }

    public SeniorXHTTPRouteBuilder primitive(String primitive) {
        this.primitive = primitive;
        return this;
    }

    public void route(Exchange exchange) {
        PropertiesComponent properties = exchange.getContext().getPropertiesComponent();
        String route = resolve(properties, url);
        if (route == null) {
            return;
        }
        if (route.endsWith("/")) {
            route = route.substring(0, route.length() - 1);
        }
        if (anonymous) {
            route += "/anonymous";
        }
        route += "/rest/" + domain //
                + '/' + service //
                + '/' + primitiveType.path //
                + '/' + primitive //
                ;

        Message message = exchange.getMessage();
        message.setHeader("route", route);
        message.setHeader("Content-Type", "application/json");
        message.setHeader(Exchange.HTTP_METHOD, method);

        exchange.setProperty("body", message.getBody());

        call(route, exchange);
    }

    private void prepare(Exchange exchange) {
        Message message = exchange.getMessage();
        message.setBody(exchange.getProperty("body"));
        LOGGER.info("Body {}", message.getBody());
        LOGGER.info("Headers {}", message.getHeaders());
    }

    private void call(String route, Exchange exchange) {
        // String endPointURI = "http://httpUrlToken?throwExceptionOnFailure=false";

        HttpComponent httpComponent = exchange.getContext().getComponent("http", HttpComponent.class);

        if (route.startsWith("https")) {
            // endPointURI = "https://httpUrlToken?throwExceptionOnFailure=false";
            httpComponent = exchange.getContext().getComponent("https", HttpComponent.class);

            if (insecure) {
                configureInsecureCall(route, httpComponent);
            }
        }
        exchange.getIn().setHeader(Exchange.HTTP_URI, route);
        try (ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate()) {
            LOGGER.info("Routing to {}", route);
            producerTemplate.request(httpComponent.createEndpoint(route), this::prepare);
            Exception e = exchange.getException();
            if (e != null) {
                throw new SeniorXHTTPException(e);
            }
        } catch (Exception e) {
            throw new SeniorXHTTPException(e);
        }
    }

    private void configureInsecureCall(String route, HttpComponent httpComponent) {
        LOGGER.warn("Routing to insecure http call {}", route);
        SSLContext sslctxt = getSSLContext();
        HttpClientConfigurer httpClientConfig = getEndpointClientConfigurer(sslctxt);
        httpComponent.setHttpClientConfigurer(httpClientConfig);
        HostnameVerifier hnv = new AllowAll();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslctxt, hnv);
        Registry<ConnectionSocketFactory> lookup = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslSocketFactory).build();
        HttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(lookup);
        httpComponent.setClientConnectionManager(connManager);
    }

    private SSLContext getSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2", "SunJSSE");

            TrustManager[] trustAllCerts = new TrustManager[] { new TrustALLManager() };
            sslContext.init(null, trustAllCerts, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
            throw new SeniorXHTTPException(e);
        }
    }

    private class TrustALLManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class AllowAll implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            LOGGER.warn("Allowing {}", hostname);
            return true;
        }
    }

    private HttpClientConfigurer getEndpointClientConfigurer(final SSLContext sslContext) {
        return clientBuilder -> clientBuilder.setSSLContext(sslContext);
    }

    private String resolve(PropertiesComponent properties, String value) {
        if (value != null && value.startsWith("{{") && value.endsWith("}}")) {
            return properties.resolveProperty(value.substring(2, value.length() - 2)).orElse(null);
        }
        return value;
    }

}
