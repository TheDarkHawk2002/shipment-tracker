package dev.okanyakin.shipmenttracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InputValidationException extends Exception {
    private final List<String> errors;

    public InputValidationException(List<String> errors) {
        super(String.join(System.lineSeparator(), errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> getErrors() { return errors; }
}
