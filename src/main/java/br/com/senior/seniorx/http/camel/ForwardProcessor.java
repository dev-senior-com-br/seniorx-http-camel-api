package br.com.senior.seniorx.http.camel;

import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardProcessor.class);

    private final Exchange source;

    public ForwardProcessor(Exchange source) {
        this.source = source;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        prepare(source, exchange);
    }

    private void prepare(Exchange src, Exchange dest) {
        Message sourceMessage = src.getMessage();
        Message message = dest.getMessage();
        message.setBody(sourceMessage.getBody());
        for (Entry<String, Object> entry : sourceMessage.getHeaders().entrySet()) {
            message.setHeader(entry.getKey(), entry.getValue());
        }
        LOGGER.info("Body {}", message.getBody());
        LOGGER.info("Headers {}", message.getHeaders());
    }

    public void reverse(Exchange exchange) {
        prepare(exchange, source);
    }

}
