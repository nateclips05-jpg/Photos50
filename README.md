# Photos50

Single-user JavaFX photo application for CS213-style assignment requirements.

## Requirements

- JDK 21 or newer with `javac`
- JavaFX SDK 21.0.2 in `tools/javafx-sdk-21.0.2`

## Project layout

- `src/` Java source
- `resources/` FXML views
- `data/` stock photos and serialized application state
- `docs/` generated Javadoc
- `tools/` local JavaFX SDK

## Scripts

- `.\compile.ps1` compiles source and copies FXML resources into `out/`
- `.\run.ps1` launches the application
- `.\verify.ps1` runs compile plus service/FXML smoke tests
- `.\generate-javadoc.ps1` generates Javadoc into `docs/`

## Default users

- `admin`
- `stock`

`stock` starts with a `stock` album backed by images in `data/stock/`.
