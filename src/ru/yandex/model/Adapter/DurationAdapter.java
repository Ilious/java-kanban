package ru.yandex.model.Adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String parseString = jsonReader.nextString();
        if (parseString.equals("null")) {
            return null;
        }

        return Duration.ofMinutes(Integer.parseInt(parseString));
    }

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (Objects.isNull(duration)) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.value(duration.toMinutes());
    }
}