$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$tmpDir = Join-Path $projectRoot "tmp"
$outDir = Join-Path $projectRoot "out"
$javaFxLib = Join-Path $projectRoot "tools\javafx-sdk-21.0.2\lib"

& (Join-Path $projectRoot "compile.ps1")
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null

@'
import photos.model.*;
import photos.service.*;
import java.nio.file.*;
import java.util.*;
public class ServiceSmokeTest {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        if (Files.exists(root)) {
            try (var walk = Files.walk(root)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.delete(path); } catch (Exception ignored) {}
                });
            }
        }
        Files.createDirectories(root.resolve("stock"));
        Files.copy(Path.of(args[1]), root.resolve("stock/stock1.png"));
        DataStore store = new DataStore(root);
        AppState state = store.load();
        PhotoLibraryService service = new PhotoLibraryService(store, state);
        service.createUser("alice");
        User alice = service.login("alice");
        Album album = service.createAlbum(alice, "vacation");
        Photo photo = service.addPhotoToAlbum(alice, album, Path.of(args[2]));
        service.updateCaption(photo, "beach");
        service.addTag(alice, photo, "person", "maya");
        service.createTagType(alice, "event", false);
        service.addTag(alice, photo, "event", "birthday");
        if (service.searchByTags(alice, "person", "maya", TagSearchOperator.SINGLE, null, null).size() != 1) throw new RuntimeException("tag search failed");
        if (service.searchByDate(alice, photo.getCaptureDate().toLocalDate(), photo.getCaptureDate().toLocalDate()).isEmpty()) throw new RuntimeException("date search failed");
        service.save();
        AppState reloaded = store.load();
        if (reloaded.getUser("alice") == null) throw new RuntimeException("reload failed");
        System.out.println("SERVICE_OK");
    }
}
'@ | Set-Content -Path (Join-Path $tmpDir "ServiceSmokeTest.java")

@'
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.util.concurrent.CountDownLatch;
public class FxmlSmokeTest {
    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] failure = new Throwable[1];
        Platform.startup(() -> {
            try {
                String[] views = {
                    "/photos/view/login.fxml",
                    "/photos/view/admin.fxml",
                    "/photos/view/user-home.fxml",
                    "/photos/view/album.fxml",
                    "/photos/view/search.fxml"
                };
                for (String view : views) {
                    Parent root = FXMLLoader.load(FxmlSmokeTest.class.getResource(view));
                    if (root == null) {
                        throw new RuntimeException("Failed to load " + view);
                    }
                }
            } catch (Throwable t) {
                failure[0] = t;
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (failure[0] != null) {
            throw new RuntimeException(failure[0]);
        }
        System.out.println("FXML_OK");
        Platform.exit();
    }
}
'@ | Set-Content -Path (Join-Path $tmpDir "FxmlSmokeTest.java")

& javac --release 21 -cp $outDir -d $tmpDir (Join-Path $tmpDir "ServiceSmokeTest.java")
& java -cp "$tmpDir;$outDir" ServiceSmokeTest (Join-Path $tmpDir "test-data") (Join-Path $projectRoot "data\stock\stock1.png") (Join-Path $projectRoot "data\stock\stock2.png")

& javac --release 21 --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -cp $outDir -d $tmpDir (Join-Path $tmpDir "FxmlSmokeTest.java")
& java --module-path $javaFxLib --add-modules javafx.controls,javafx.fxml -cp "$tmpDir;$outDir" FxmlSmokeTest

Write-Host "Verification complete."
