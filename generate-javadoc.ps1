$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$docsDir = Join-Path $projectRoot "docs"
$javaFxLib = Join-Path $projectRoot "tools\javafx-sdk-21.0.2\lib"
$srcDir = Join-Path $projectRoot "src"

New-Item -ItemType Directory -Force -Path $docsDir | Out-Null

& javadoc -quiet -Xdoclint:none --release 21 --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml `
    -d $docsDir `
    -sourcepath $srcDir `
    -subpackages photos

Write-Host "Javadoc written to $docsDir"
