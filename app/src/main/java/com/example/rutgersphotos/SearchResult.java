package com.example.rutgersphotos;

public final class SearchResult {
    private final int albumIndex;
    private final int photoIndex;
    private final String albumName;
    private final PhotoItem photo;

    public SearchResult(int albumIndex, int photoIndex, String albumName, PhotoItem photo) {
        this.albumIndex = albumIndex;
        this.photoIndex = photoIndex;
        this.albumName = albumName;
        this.photo = photo;
    }

    public int getAlbumIndex() {
        return albumIndex;
    }

    public int getPhotoIndex() {
        return photoIndex;
    }

    public String getAlbumName() {
        return albumName;
    }

    public PhotoItem getPhoto() {
        return photo;
    }
}
