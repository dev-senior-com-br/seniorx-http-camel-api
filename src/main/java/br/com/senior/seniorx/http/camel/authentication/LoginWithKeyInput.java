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
public class LoginWithKeyInput {

    public static final JacksonDataFormat LOGIN_WITH_KEY_INPUT_FORMAT = new JacksonDataFormat(LoginWithKeyInput.class);

    @JsonProperty("accessKey")
    public String accessKey;
    @JsonProperty("secret")
    public String secret;
    @JsonProperty("tenantName")
    public String tenantName;

}
