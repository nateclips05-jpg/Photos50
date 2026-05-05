package com.example.rutgersphotos;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public final class PhotoActivity extends Activity {
    public static final String EXTRA_PHOTO_INDEX = "photoIndex";

    private PhotoLibrary library;
    private Album album;
    private int albumIndex;
    private int photoIndex;
    private int selectedTagIndex = -1;
    private ImageView fullPhoto;
    private TextView photoPosition;
    private ListView tagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        library = LibraryRepository.get(this);
        albumIndex = getIntent().getIntExtra(AlbumActivity.EXTRA_ALBUM_INDEX, -1);
        photoIndex = getIntent().getIntExtra(EXTRA_PHOTO_INDEX, -1);
        if (!loadAlbumAndPhoto()) {
            finish();
            return;
        }

        fullPhoto = findViewById(R.id.fullPhoto);
        photoPosition = findViewById(R.id.photoPosition);
        tagList = findViewById(R.id.tagList);
        Button previousButton = findViewById(R.id.previousButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button addTagButton = findViewById(R.id.addTagButton);
        Button deleteTagButton = findViewById(R.id.deleteTagButton);

        tagList.setOnItemClickListener((parent, view, position, id) -> selectedTagIndex = position);
        previousButton.setOnClickListener(view -> showPreviousPhoto());
        nextButton.setOnClickListener(view -> showNextPhoto());
        addTagButton.setOnClickListener(view -> addTag());
        deleteTagButton.setOnClickListener(view -> deleteTag());

        refreshPhoto();
    }

    private boolean loadAlbumAndPhoto() {
        if (albumIndex < 0 || albumIndex >= library.getAlbums().size()) {
            return false;
        }
        album = library.getAlbum(albumIndex);
        return photoIndex >= 0 && photoIndex < album.getPhotos().size();
    }

    private void refreshPhoto() {
        PhotoItem photo = album.getPhoto(photoIndex);
        fullPhoto.setImageURI(Uri.parse(photo.getUri()));
        photoPosition.setText(album.getName() + " - " + (photoIndex + 1) + " of " + album.getPhotos().size());

        ArrayList<String> labels = new ArrayList<>();
        for (Tag tag : photo.getTags()) {
            labels.add(tag.toString());
        }
        if (labels.isEmpty()) {
            labels.add("No tags");
        }
        tagList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, labels));
        selectedTagIndex = -1;
    }

    private void showPreviousPhoto() {
        if (album.getPhotos().isEmpty()) {
            return;
        }
        photoIndex = photoIndex == 0 ? album.getPhotos().size() - 1 : photoIndex - 1;
        refreshPhoto();
    }

    private void showNextPhoto() {
        if (album.getPhotos().isEmpty()) {
            return;
        }
        photoIndex = (photoIndex + 1) % album.getPhotos().size();
        refreshPhoto();
    }

    private void addTag() {
        DialogHelper.showTagDialog(this, (type, value) -> {
            try {
                album.getPhoto(photoIndex).addTag(new Tag(type, value));
                LibraryRepository.save(this);
                refreshPhoto();
            } catch (IllegalArgumentException exception) {
                DialogHelper.showMessage(this, exception.getMessage());
            }
        });
    }

    private void deleteTag() {
        PhotoItem photo = album.getPhoto(photoIndex);
        if (photo.getTags().isEmpty()) {
            DialogHelper.showMessage(this, "This photo has no tags.");
            return;
        }
        if (selectedTagIndex < 0 || selectedTagIndex >= photo.getTags().size()) {
            DialogHelper.showMessage(this, "Select a tag first.");
            return;
        }
        String tag = photo.getTags().get(selectedTagIndex).toString();
        new AlertDialog.Builder(this)
                .setTitle("Delete tag")
                .setMessage("Delete " + tag + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    photo.removeTag(selectedTagIndex);
                    LibraryRepository.save(this);
                    refreshPhoto();
                })
                .show();
    }
}
