package com.example.rutgersphotos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public final class SearchActivity extends Activity {
    private PhotoLibrary library;
    private Spinner typeOneSpinner;
    private Spinner operatorSpinner;
    private Spinner typeTwoSpinner;
    private AutoCompleteTextView valueOneInput;
    private AutoCompleteTextView valueTwoInput;
    private GridView resultsGrid;
    private final ArrayList<SearchResult> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        library = LibraryRepository.get(this);
        typeOneSpinner = findViewById(R.id.typeOneSpinner);
        operatorSpinner = findViewById(R.id.operatorSpinner);
        typeTwoSpinner = findViewById(R.id.typeTwoSpinner);
        valueOneInput = findViewById(R.id.valueOneInput);
        valueTwoInput = findViewById(R.id.valueTwoInput);
        Button runSearchButton = findViewById(R.id.runSearchButton);
        resultsGrid = findViewById(R.id.searchResultsGrid);

        typeOneSpinner.setAdapter(SimpleAdapters.tagTypeAdapter(this));
        typeTwoSpinner.setAdapter(SimpleAdapters.tagTypeAdapter(this));
        operatorSpinner.setAdapter(SimpleAdapters.operatorAdapter(this));

        attachAutocomplete(valueOneInput, typeOneSpinner);
        attachAutocomplete(valueTwoInput, typeTwoSpinner);

        runSearchButton.setOnClickListener(view -> runSearch());
        resultsGrid.setOnItemClickListener((parent, view, position, id) -> openResult(position));
        refreshResults();
    }

    private void attachAutocomplete(AutoCompleteTextView input, Spinner typeSpinner) {
        input.setThreshold(1);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                String prefix = text.toString().trim();
                if (prefix.isEmpty()) {
                    return;
                }
                try {
                    List<String> suggestions = library.suggestionsFor(typeSpinner.getSelectedItem().toString(), prefix);
                    input.setAdapter(new ArrayAdapter<>(
                            SearchActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                    ));
                    input.showDropDown();
                } catch (IllegalArgumentException ignored) {
                    input.dismissDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void runSearch() {
        String operator = operatorSpinner.getSelectedItem().toString();
        try {
            results.clear();
            results.addAll(library.search(
                    typeOneSpinner.getSelectedItem().toString(),
                    valueOneInput.getText().toString(),
                    operator,
                    typeTwoSpinner.getSelectedItem().toString(),
                    valueTwoInput.getText().toString()
            ));
            refreshResults();
            if (results.isEmpty()) {
                DialogHelper.showMessage(this, "No matching photos.");
            }
        } catch (IllegalArgumentException exception) {
            DialogHelper.showMessage(this, exception.getMessage());
        }
    }

    private void refreshResults() {
        resultsGrid.setAdapter(new SearchResultAdapter(this, results));
    }

    private void openResult(int position) {
        SearchResult result = results.get(position);
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_ALBUM_INDEX, result.getAlbumIndex());
        intent.putExtra(PhotoActivity.EXTRA_PHOTO_INDEX, result.getPhotoIndex());
        startActivity(intent);
    }
}
