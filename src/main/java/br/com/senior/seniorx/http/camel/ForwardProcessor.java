package br.com.senior.seniorx.http.camel;

import java.util.function.BiConsumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ForwardProcessor implements Processor {

    private final Exchange source;
    private final BiConsumer<Exchange, Exchange> forwarder;

    public ForwardProcessor(Exchange source, BiConsumer<Exchange, Exchange> forwarder) {
        this.source = source;
        this.forwarder = forwarder;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        forwarder.accept(source, exchange);
    }

}
