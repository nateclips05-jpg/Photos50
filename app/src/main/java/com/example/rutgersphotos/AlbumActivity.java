package com.example.rutgersphotos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public final class AlbumActivity extends Activity {
    public static final String EXTRA_ALBUM_INDEX = "albumIndex";
    private static final int REQUEST_PHOTO = 1001;

    private PhotoLibrary library;
    private Album album;
    private int albumIndex;
    private int selectedPhotoIndex = -1;
    private GridView photoGrid;
    private TextView albumTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        library = LibraryRepository.get(this);
        albumIndex = getIntent().getIntExtra(EXTRA_ALBUM_INDEX, -1);
        if (albumIndex < 0 || albumIndex >= library.getAlbums().size()) {
            finish();
            return;
        }
        album = library.getAlbum(albumIndex);

        albumTitle = findViewById(R.id.albumTitle);
        photoGrid = findViewById(R.id.photoGrid);
        Button addButton = findViewById(R.id.addPhotoButton);
        Button displayButton = findViewById(R.id.displayPhotoButton);
        Button moveButton = findViewById(R.id.movePhotoButton);
        Button removeButton = findViewById(R.id.removePhotoButton);

        photoGrid.setOnItemClickListener((parent, view, position, id) -> selectedPhotoIndex = position);
        photoGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            openPhoto(position);
            return true;
        });

        addButton.setOnClickListener(view -> choosePhoto());
        displayButton.setOnClickListener(view -> {
            if (requirePhotoSelection()) {
                openPhoto(selectedPhotoIndex);
            }
        });
        moveButton.setOnClickListener(view -> movePhoto());
        removeButton.setOnClickListener(view -> removePhoto());

        refreshPhotos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        library = LibraryRepository.get(this);
        if (albumIndex >= 0 && albumIndex < library.getAlbums().size()) {
            album = library.getAlbum(albumIndex);
            refreshPhotos();
        }
    }

    private void refreshPhotos() {
        albumTitle.setText(album.getName() + " (" + album.getPhotos().size() + ")");
        photoGrid.setAdapter(new PhotoGridAdapter(this, album.getPhotos()));
        if (selectedPhotoIndex >= album.getPhotos().size()) {
            selectedPhotoIndex = -1;
        }
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(uri, flags);
            } catch (SecurityException ignored) {
                DialogHelper.showMessage(this, "Photo permission is temporary for this run.");
            }
            try {
                album.addPhoto(new PhotoItem(uri.toString()));
                LibraryRepository.save(this);
                refreshPhotos();
            } catch (IllegalArgumentException exception) {
                DialogHelper.showMessage(this, exception.getMessage());
            }
        }
    }

    private void removePhoto() {
        if (!requirePhotoSelection()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Remove photo")
                .setMessage("Remove the selected photo from this album?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    album.removePhoto(selectedPhotoIndex);
                    selectedPhotoIndex = -1;
                    LibraryRepository.save(this);
                    refreshPhotos();
                })
                .show();
    }

    private void movePhoto() {
        if (!requirePhotoSelection()) {
            return;
        }
        ArrayList<String> targetNames = new ArrayList<>();
        ArrayList<Integer> targetIndexes = new ArrayList<>();
        for (int i = 0; i < library.getAlbums().size(); i++) {
            if (i != albumIndex) {
                targetNames.add(library.getAlbum(i).getName());
                targetIndexes.add(i);
            }
        }
        if (targetIndexes.isEmpty()) {
            DialogHelper.showMessage(this, "Create another album before moving a photo.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Move to album")
                .setItems(targetNames.toArray(new String[0]), (dialog, which) -> {
                    try {
                        library.movePhoto(albumIndex, selectedPhotoIndex, targetIndexes.get(which));
                        selectedPhotoIndex = -1;
                        LibraryRepository.save(this);
                        refreshPhotos();
                    } catch (IllegalArgumentException exception) {
                        DialogHelper.showMessage(this, exception.getMessage());
                    }
                })
                .show();
    }

    private void openPhoto(int index) {
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra(EXTRA_ALBUM_INDEX, albumIndex);
        intent.putExtra(PhotoActivity.EXTRA_PHOTO_INDEX, index);
        startActivity(intent);
    }

    private boolean requirePhotoSelection() {
        if (selectedPhotoIndex < 0 || selectedPhotoIndex >= album.getPhotos().size()) {
            DialogHelper.showMessage(this, "Select a photo first.");
            return false;
        }
        return true;
    }
}
