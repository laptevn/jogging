package com.laptevn.jogging.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

class DoubleSerializer extends JsonSerializer<Double> {
    private final static DecimalFormat FORMAT = new DecimalFormat("#.##");

    @Override
    public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {

        jsonGenerator.writeNumber(FORMAT.format(value));
    }
}