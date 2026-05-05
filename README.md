# Rutgers Photos Android

Android port of the JavaFX Photos assignment. The app is written in Java with Android XML layouts and stores library data as JSON in the app's internal files directory.

## Build

Open this folder in Android Studio:

```text
RutgersPhotosAndroid
```

Use a 1080 x 2400, 420 dpi emulator such as Pixel 6 or Medium Phone with API 36 or API 37. The Gradle files use Kotlin DSL, as requested by the assignment.

## Features

- Home screen loads saved JSON data and lists albums in plain text.
- Albums can be created, opened, renamed, and deleted.
- Open albums display photo thumbnails.
- Photos can be added through Android's image picker, removed, displayed, and moved to another album.
- The photo display screen shows tags and provides Previous/Next manual slideshow controls.
- Tags are limited to `person` and `location`; tags can be added and deleted.
- Search runs across all albums and supports single tag search, AND, OR, case-insensitive matching, and prefix autocomplete.

## JSON storage

The app writes `photo-library.json` inside Android internal app storage. Each album stores its name and an array of photo records. Each photo stores its Android URI and an array of tag objects:

```json
{
  "albums": [
    {
      "name": "Vacation",
      "photos": [
        {
          "uri": "content://...",
          "tags": [
            {"type": "location", "value": "new york"}
          ]
        }
      ]
    }
  ]
}
```