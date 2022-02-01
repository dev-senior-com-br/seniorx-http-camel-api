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
public class LoginOutput {

    public static final JacksonDataFormat LOGIN_OUTPUT_FORMAT = new JacksonDataFormat(LoginOutput.class);

    // Success response

    @JsonProperty("jsonToken")
    public String jsonToken;

    // Error response

    @JsonProperty("message")
    public String message;
    @JsonProperty("reason")
    public String reason;

    @Override
    public String toString() {
        return "LoginOutput [jsonToken=" + jsonToken + ", message=" + message + ", reason=" + reason + "]";
    }

}
