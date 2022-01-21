package br.com.senior.seniorx.http.camel.authentication;

import static br.com.senior.seniorx.http.camel.authentication.LoginInput.LOGIN_INPUT_FORMAT;
import static br.com.senior.seniorx.http.camel.authentication.LoginWithKeyInput.LOGIN_WITH_KEY_INPUT_FORMAT;
import static br.com.senior.seniorx.http.camel.authentication.RefreshTokenInput.REFRESH_TOKEN_INPUT_FORMAT;
import static org.apache.camel.ExchangePattern.InOut;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.config.units.MemoryUnit.B;

import java.time.Duration;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.senior.seniorx.http.camel.SeniorXHTTPRouteBuilder;

public class AuthenticationAPI {

    private static final String PLATFORM = "platform";
    private static final String AUTHENTICATION = "authentication";
    private static final String ACTION = "action";

    private static final String AUTHENTICATE = "authenticate";
    private static final String HEADERS_LOG = "${in.headers}";

    private static final String DIRECT_TOKEN_FOUND = "direct:authentication-token-found";
    private static final String DIRECT_TOKEN_NOT_FOUND = "direct:authentication-token-not-found";
    private static final String DIRECT_LOGIN = "direct:authentication-login";
    private static final String DIRECT_LOGIN_WITH_KEY = "direct:authentication-login-with-key";
    private static final String DIRECT_REFRESH_TOKEN = "direct:authentication-refresh-token";
    private static final String TOKEN_CACHE_KEY = "token-cache-key";
    private static final String TOKEN = "token";

    private static final String TOKEN_CACHE_NAME = "tokenCache";
    // Token cache size in bytes.
    private static final long TOKEN_CACHE_SIZE = 64000000;
    // Refresh token TTL in seconds (See environment variable KONG_REFRESH_TOKEN_TTL at https://git.senior.com.br/arquitetura/kong-rest-client/-/wikis/home).
    private static final int REFRESH_TOKEN_TTL = 15552000;
    // Token expiration time margin in seconds.
    private static final int TOKEN_EXPIRATION_MARGIN = 60;

    private static final CacheManager CACHE_MANAGER = newCacheManagerBuilder().build(true);

    private static final Cache<String, Token> TOKEN_CACHE = CACHE_MANAGER.createCache(TOKEN_CACHE_NAME, //
            newCacheConfigurationBuilder(String.class, Token.class, //
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(TOKEN_CACHE_SIZE, B).build()) //
            .withExpiry(timeToLiveExpiration(Duration.ofSeconds(REFRESH_TOKEN_TTL))) //
            .build());

    private final RouteBuilder builder;

    public AuthenticationAPI(RouteBuilder builder) {
        this.builder = builder;
    }

    public void authenticate(String from, Processor enrichWithToken, String to) {
        tokenFound();

        tokenNotFound();

        login();

        loginWithKey();

        refreshToken();

        builder //
        .from(from) //
        .routeId(AUTHENTICATE) //
        .to("log:authenticate") //
        .log(HEADERS_LOG) //

        .process(this::searchToken) //

        .choice() // Token found
        .when(builder.method(this, "tokenFound")) //

        .setExchangePattern(InOut) //
        .to(DIRECT_TOKEN_FOUND) //

        .otherwise() // Token not found

        .setExchangePattern(InOut) //
        .to(DIRECT_TOKEN_NOT_FOUND) //

        .end() // Token found

        .process(enrichWithToken) //
        .to(to) //
        ;
    }

    public static void addAuthorization(Exchange exchange) {
        Token token = (Token) exchange.getProperty(TOKEN);
        exchange.getMessage().setHeader("Authorization", "Bearer " + token.accessToken);
    }

    private void tokenFound() {
        builder //
        .from(DIRECT_TOKEN_FOUND) //
        .routeId("token-found") //
        .to("log:tokenFound") //
        .log(HEADERS_LOG) //

        .choice() // Expired token
        .when(builder.method(this, "isExpiredToken")) //

        .to("log:tokenExpired") //
        .log(HEADERS_LOG) //

        .setExchangePattern(InOut) //
        .to(DIRECT_REFRESH_TOKEN) //

        .to("log:refreshedToken") //
        .log(HEADERS_LOG) //
        .unmarshal(LoginOutput.LOGIN_OUTPUT_FORMAT) //
        .process(this::unmarshallToken) //

        .end() // Expired token
        ;
    }

    private void tokenNotFound() {
        builder //
        .from(DIRECT_TOKEN_NOT_FOUND) //
        .routeId("token-not-found") //
        .to("log:tokenNotFound") //
        .log(HEADERS_LOG) //

        .choice() // User login
        .when(builder.method(this, "isUserLogin")) //

        .setExchangePattern(InOut) //
        .to(DIRECT_LOGIN) //

        .otherwise() // Application login

        .setExchangePattern(InOut) //
        .to(DIRECT_LOGIN_WITH_KEY) //

        .end() // User login

        .to("log:authenticated") //
        .log(HEADERS_LOG) //
        .unmarshal(LoginOutput.LOGIN_OUTPUT_FORMAT) //
        .process(this::unmarshallToken) //
        ;
    }

    private void login() {
        SeniorXHTTPRouteBuilder login = new SeniorXHTTPRouteBuilder(builder);
        login //
        .domain(PLATFORM) //
        .service(AUTHENTICATION) //
        .primitiveType(ACTION) // .
        .primitive("login");

        builder //
        .from(DIRECT_LOGIN) //
        .routeId("login") //
        .marshal(LOGIN_INPUT_FORMAT) //
        .to("log:login") //
        .log(HEADERS_LOG) //
        .setExchangePattern(InOut) //
        .dynamicRouter(login.route()) //
        .to("log:logged") //
        .log(HEADERS_LOG) //
        ;
    }

    private void loginWithKey() {
        SeniorXHTTPRouteBuilder loginWithKey = new SeniorXHTTPRouteBuilder(builder);
        loginWithKey //
        .domain(PLATFORM) //
        .service(AUTHENTICATION) //
        .primitiveType(ACTION) //
        .primitive("loginWithKey") //
        .anonymous(true);

        builder //
        .from(DIRECT_LOGIN_WITH_KEY) //
        .routeId("loginWithKey") //
        .marshal(LOGIN_WITH_KEY_INPUT_FORMAT) //
        .to("log:loginWithKey") //
        .log(HEADERS_LOG) //
        .setExchangePattern(InOut) //
        .dynamicRouter(loginWithKey.route()) //
        .to("log:loggedWithKey") //
        .log(HEADERS_LOG) //
        ;
    }

    private void refreshToken() {
        SeniorXHTTPRouteBuilder refreshToken = new SeniorXHTTPRouteBuilder(builder);
        refreshToken //
        .domain(PLATFORM) //
        .service(AUTHENTICATION) //
        .primitiveType(ACTION) // .
        .primitive("refreshToken");

        builder //
        .from(DIRECT_REFRESH_TOKEN) //
        .routeId("refreshToken") //
        .process(this::prepareRefreshToken) //
        .marshal(REFRESH_TOKEN_INPUT_FORMAT) //
        .to("log:refreshToken") //
        .log(HEADERS_LOG) //
        .setExchangePattern(InOut) //
        .dynamicRouter(refreshToken.route()) //
        .to("log:refreshedToken") //
        .log(HEADERS_LOG) //
        ;
    }

    private void searchToken(Exchange exchange) {
        String key = null;
        Token token = null;
        Object body = exchange.getMessage().getBody();
        if (body instanceof LoginInput) {
            LoginInput loginInput = (LoginInput) body;
            key = "user:" + loginInput.username + '$' + loginInput.password;
        } else if (body instanceof LoginWithKeyInput) {
            LoginWithKeyInput loginWithKeyInput = (LoginWithKeyInput) body;
            key = "app:" + loginWithKeyInput.accessKey + '@' + loginWithKeyInput.tenantName + '$' + loginWithKeyInput.secret;
        } else {
            throw new AuthenticationException("Unknown login payload: " + body.getClass().getName());
        }
        exchange.setProperty(TOKEN_CACHE_KEY, key);
        token = TOKEN_CACHE.get(key);
        if (token != null) {
            exchange.getMessage().setBody(token);
        }
    }

    public boolean tokenFound(Object body) {
        return body instanceof Token;
    }

    public boolean isExpiredToken(Object body) {
        Token token = (Token) body;
        return now() >= token.expireTime;
    }

    public boolean isUserLogin(Object body) {
        return body instanceof LoginInput;
    }

    private void unmarshallToken(Exchange exchange) {
        LoginOutput output = (LoginOutput) exchange.getMessage().getBody();
        if (output.jsonToken == null) {
            throw new AuthenticationException(output.reason + ": " + output.message);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            Token token = mapper.readValue(output.jsonToken, Token.class);
            token.expireTime = now() + ((token.expiresIn - TOKEN_EXPIRATION_MARGIN) * 1000);
            TOKEN_CACHE.put(exchange.getProperty(TOKEN_CACHE_KEY).toString(), token);
            exchange.setProperty(TOKEN, token);
            exchange.getMessage().setBody(token);
            addAuthorization(exchange);
        } catch (JsonProcessingException e) {
            throw new AuthenticationException(e);
        }
    }

    private long now() {
        return new Date().getTime();
    }

    private void prepareRefreshToken(Exchange exchange) {
        RefreshTokenInput input = new RefreshTokenInput();
        Token token = (Token) exchange.getProperty(TOKEN);
        input.refreshToken = token.refreshToken;
        exchange.getMessage().setBody(input);
    }

}
