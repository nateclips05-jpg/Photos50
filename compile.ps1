$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaFxLib = Join-Path $projectRoot "tools\javafx-sdk-21.0.2\lib"
$outDir = Join-Path $projectRoot "out"
$srcFiles = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not (Test-Path $javaFxLib)) {
    throw "JavaFX SDK not found at $javaFxLib"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

& javac --release 21 --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -d $outDir $srcFiles
Copy-Item -Path (Join-Path $projectRoot "resources\*") -Destination $outDir -Recurse -Force

Write-Host "Compiled to $outDir"
