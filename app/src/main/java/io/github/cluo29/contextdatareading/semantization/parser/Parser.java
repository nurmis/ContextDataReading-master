package io.github.cluo29.contextdatareading.semantization.parser;

import io.github.cluo29.contextdatareading.AbstractEvent;

import java.util.Map;


public interface Parser {

    Map<String, String> parseEvent(AbstractEvent event);

}
