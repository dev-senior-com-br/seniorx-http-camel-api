package br.com.senior.seniorx.http.camel.authentication;

import org.apache.camel.component.jackson.JacksonDataFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(serialization = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Token {

    public static final JacksonDataFormat TOKEN_FORMAT = new JacksonDataFormat(Token.class);

    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("expires_in")
    public Long expiresIn;
    @JsonProperty("expire_time")
    public Long expireTime;

    @Override
    public String toString() {
        String at = accessToken == null ? null : accessToken.isEmpty() ? accessToken : "?";
        String rt = refreshToken == null ? null : refreshToken.isEmpty() ? refreshToken : "?";
        return "Token [accessToken=" + at + ", refreshToken=" + rt + ", expiresIn=" + expiresIn + ", expireTime=" + expireTime + "]";
    }

}
