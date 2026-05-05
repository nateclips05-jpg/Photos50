package com.example.rutgersphotos;

import android.content.Context;
import android.widget.ArrayAdapter;

public final class SimpleAdapters {
    private SimpleAdapters() {
    }

    public static ArrayAdapter<String> tagTypeAdapter(Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                new String[]{Tag.PERSON, Tag.LOCATION}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    public static ArrayAdapter<String> operatorAdapter(Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                new String[]{"SINGLE", "AND", "OR"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}
