package ru.yandex.model.Adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class InstantAdapter extends TypeAdapter<Instant> {

    @Override
    public Instant read(JsonReader jsonReader) throws IOException {
        String parseString = jsonReader.nextString();
        if (parseString.equals("null")) {
            return null;
        }

        return Instant.parse(parseString);
    }

    @Override
    public void write(JsonWriter jsonWriter, Instant instant) throws IOException {
        if (Objects.isNull(instant)) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(instant.toString());
    }
}
