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
public class RefreshTokenInput {

    public static final JacksonDataFormat REFRESH_TOKEN_INPUT_FORMAT = new JacksonDataFormat(RefreshTokenInput.class);

    @JsonProperty("refreshToken")
    public String refreshToken;

    @Override
    public String toString() {
        return "RefreshTokenInput [refreshToken=" + refreshToken + "]";
    }

}
