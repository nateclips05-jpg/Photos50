package com.example.rutgersphotos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public final class MainActivity extends Activity {
    private PhotoLibrary library;
    private ListView albumList;
    private int selectedAlbumIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        library = LibraryRepository.get(this);
        albumList = findViewById(R.id.albumList);
        Button openButton = findViewById(R.id.openAlbumButton);
        Button searchButton = findViewById(R.id.searchButton);
        Button createButton = findViewById(R.id.createAlbumButton);
        Button renameButton = findViewById(R.id.renameAlbumButton);
        Button deleteButton = findViewById(R.id.deleteAlbumButton);

        albumList.setOnItemClickListener((parent, view, position, id) -> selectedAlbumIndex = position);
        albumList.setOnItemLongClickListener((parent, view, position, id) -> {
            openAlbum(position);
            return true;
        });

        openButton.setOnClickListener(view -> {
            if (requireAlbumSelection()) {
                openAlbum(selectedAlbumIndex);
            }
        });
        searchButton.setOnClickListener(view -> startActivity(new Intent(this, SearchActivity.class)));
        createButton.setOnClickListener(view -> createAlbum());
        renameButton.setOnClickListener(view -> renameAlbum());
        deleteButton.setOnClickListener(view -> deleteAlbum());

        refreshAlbums();
    }

    @Override
    protected void onResume() {
        super.onResume();
        library = LibraryRepository.get(this);
        refreshAlbums();
    }

    private void refreshAlbums() {
        ArrayList<String> labels = new ArrayList<>();
        for (Album album : library.getAlbums()) {
            labels.add(album.getName());
        }
        albumList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, labels));
        if (selectedAlbumIndex >= library.getAlbums().size()) {
            selectedAlbumIndex = -1;
        }
        if (selectedAlbumIndex >= 0) {
            albumList.setItemChecked(selectedAlbumIndex, true);
        }
    }

    private void createAlbum() {
        DialogHelper.showTextDialog(this, "Create album", "", value -> {
            try {
                library.createAlbum(value);
                LibraryRepository.save(this);
                refreshAlbums();
            } catch (IllegalArgumentException exception) {
                DialogHelper.showMessage(this, exception.getMessage());
            }
        });
    }

    private void renameAlbum() {
        if (!requireAlbumSelection()) {
            return;
        }
        String currentName = library.getAlbum(selectedAlbumIndex).getName();
        DialogHelper.showTextDialog(this, "Rename album", currentName, value -> {
            try {
                library.renameAlbum(selectedAlbumIndex, value);
                LibraryRepository.save(this);
                refreshAlbums();
            } catch (IllegalArgumentException exception) {
                DialogHelper.showMessage(this, exception.getMessage());
            }
        });
    }

    private void deleteAlbum() {
        if (!requireAlbumSelection()) {
            return;
        }
        String name = library.getAlbum(selectedAlbumIndex).getName();
        new AlertDialog.Builder(this)
                .setTitle("Delete album")
                .setMessage("Delete " + name + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    library.deleteAlbum(selectedAlbumIndex);
                    selectedAlbumIndex = -1;
                    LibraryRepository.save(this);
                    refreshAlbums();
                })
                .show();
    }

    private void openAlbum(int index) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_ALBUM_INDEX, index);
        startActivity(intent);
    }

    private boolean requireAlbumSelection() {
        if (selectedAlbumIndex < 0 || selectedAlbumIndex >= library.getAlbums().size()) {
            DialogHelper.showMessage(this, "Select an album first.");
            return false;
        }
        return true;
    }
}
