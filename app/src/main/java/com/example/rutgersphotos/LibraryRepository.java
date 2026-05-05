package com.example.rutgersphotos;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

public final class LibraryRepository {
    private static PhotoLibrary library;

    private LibraryRepository() {
    }

    public static PhotoLibrary get(Context context) {
        if (library == null) {
            try {
                library = PhotoLibrary.load(context.getApplicationContext());
            } catch (IOException | JSONException exception) {
                library = new PhotoLibrary();
                Toast.makeText(context, "Could not load saved library. Starting empty.", Toast.LENGTH_LONG).show();
            }
        }
        return library;
    }

    public static void save(Context context) {
        try {
            get(context).save(context.getApplicationContext());
        } catch (IOException | JSONException exception) {
            Toast.makeText(context, "Could not save library.", Toast.LENGTH_LONG).show();
        }
    }
}
