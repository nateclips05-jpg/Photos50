package com.example.rutgersphotos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public final class DialogHelper {
    public interface TextCallback {
        void onText(String value);
    }

    public interface TagCallback {
        void onTag(String type, String value);
    }

    private DialogHelper() {
    }

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showTextDialog(Context context, String title, String initialValue, TextCallback callback) {
        EditText input = new EditText(context);
        input.setSingleLine(true);
        input.setText(initialValue == null ? "" : initialValue);
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> callback.onText(input.getText().toString()))
                .show();
    }

    public static void showTagDialog(Context context, TagCallback callback) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(context, 16);
        layout.setPadding(padding, padding / 2, padding, 0);

        Spinner typeSpinner = new Spinner(context);
        typeSpinner.setAdapter(SimpleAdapters.tagTypeAdapter(context));
        EditText valueInput = new EditText(context);
        valueInput.setSingleLine(true);
        valueInput.setHint("Tag value");

        layout.addView(typeSpinner);
        layout.addView(valueInput);

        new AlertDialog.Builder(context)
                .setTitle("Add tag")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> callback.onTag(
                        typeSpinner.getSelectedItem().toString(),
                        valueInput.getText().toString()
                ))
                .show();
    }

    public static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static LinearLayout verticalDialogLayout(Context context, View... children) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(context, 16);
        layout.setPadding(padding, padding / 2, padding, 0);
        for (View child : children) {
            layout.addView(child);
        }
        return layout;
    }
}
