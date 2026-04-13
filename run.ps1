$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaFxLib = Join-Path $projectRoot "tools\javafx-sdk-21.0.2\lib"
$outDir = Join-Path $projectRoot "out"

if (-not (Test-Path $outDir)) {
    & (Join-Path $projectRoot "compile.ps1")
}

Push-Location $projectRoot
try {
    & java --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -cp $outDir photos.Photos
} finally {
    Pop-Location
}
