package photos.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import photos.Photos;
import photos.model.Photo;
import photos.model.TagSearchOperator;
import photos.model.TagType;
import photos.util.DialogUtils;
import photos.util.PhotoListCell;

import java.util.List;

/**
 * Handles the photo search screen.
 * The user can search either by a date range or by one or two tag-value pairs,
 * then save the matching photos into a new album.
 *
 * @author Owner
 */
public final class SearchController implements ScreenController {
    @FXML
    private RadioButton dateModeRadio;
    @FXML
    private RadioButton tagModeRadio;
    @FXML
    private ToggleGroup searchModeGroup;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<TagType> tagTypeOneComboBox;
    @FXML
    private TextField tagValueOneField;
    @FXML
    private ComboBox<TagSearchOperator> operatorComboBox;
    @FXML
    private ComboBox<TagType> tagTypeTwoComboBox;
    @FXML
    private TextField tagValueTwoField;
    @FXML
    private ListView<Photo> resultsListView;
    @FXML
    private Label resultsCountLabel;

    private Photos app;
    private boolean initialized;

    @Override
    public void setApp(Photos app) {
        this.app = app;
    }

    @Override
    public void onShow() {
        if (!initialized) {
            resultsListView.setCellFactory(list -> new PhotoListCell());
            operatorComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateModeState());
            searchModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> updateModeState());
            initialized = true;
        }
        tagTypeOneComboBox.setItems(FXCollections.observableArrayList(app.getCurrentUser().getTagTypes()));
        tagTypeTwoComboBox.setItems(FXCollections.observableArrayList(app.getCurrentUser().getTagTypes()));
        operatorComboBox.setItems(FXCollections.observableArrayList(TagSearchOperator.values()));
        operatorComboBox.getSelectionModel().select(TagSearchOperator.SINGLE);
        if (!tagTypeOneComboBox.getItems().isEmpty()) {
            tagTypeOneComboBox.getSelectionModel().selectFirst();
            tagTypeTwoComboBox.getSelectionModel().selectFirst();
        }
        dateModeRadio.setSelected(true);
        updateModeState();
        resultsListView.setItems(FXCollections.observableArrayList(app.getLastSearchResults()));
        updateCount();
    }

    @FXML
    private void handleSearch() {
        try {
            List<Photo> results;
            if (dateModeRadio.isSelected()) {
                results = app.getService().searchByDate(app.getCurrentUser(), startDatePicker.getValue(), endDatePicker.getValue());
            } else {
                TagType firstType = tagTypeOneComboBox.getSelectionModel().getSelectedItem();
                TagType secondType = tagTypeTwoComboBox.getSelectionModel().getSelectedItem();
                results = app.getService().searchByTags(
                        app.getCurrentUser(),
                        firstType == null ? null : firstType.getName(),
                        tagValueOneField.getText(),
                        operatorComboBox.getSelectionModel().getSelectedItem(),
                        secondType == null ? null : secondType.getName(),
                        tagValueTwoField.getText()
                );
            }
            app.setLastSearchResults(results);
            resultsListView.setItems(FXCollections.observableArrayList(results));
            updateCount();
        } catch (Exception exception) {
            DialogUtils.showError("Search Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleCreateAlbumFromResults() {
        if (resultsListView.getItems().isEmpty()) {
            DialogUtils.showError("No Results", "Run a search with at least one result first.");
            return;
        }
        DialogUtils.promptText("Create Album", "Album name:", "")
                .ifPresent(name -> {
                    try {
                        app.getService().createAlbumFromResults(app.getCurrentUser(), name, resultsListView.getItems());
                        app.save();
                        DialogUtils.showInfo("Album Created", "Created album from current search results.");
                    } catch (Exception exception) {
                        DialogUtils.showError("Create Album Failed", exception.getMessage());
                    }
                });
    }

    @FXML
    private void handleBack() {
        try {
            app.showUserHome();
        } catch (Exception exception) {
            DialogUtils.showError("Navigation Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            app.logout();
        } catch (Exception exception) {
            DialogUtils.showError("Logout Failed", exception.getMessage());
        }
    }

    private void updateModeState() {
        boolean dateMode = dateModeRadio.isSelected();
        boolean single = operatorComboBox.getValue() == TagSearchOperator.SINGLE;
        startDatePicker.setDisable(!dateMode);
        endDatePicker.setDisable(!dateMode);
        tagTypeOneComboBox.setDisable(dateMode);
        tagValueOneField.setDisable(dateMode);
        operatorComboBox.setDisable(dateMode);
        tagTypeTwoComboBox.setDisable(dateMode || single);
        tagValueTwoField.setDisable(dateMode || single);
    }

    private void updateCount() {
        resultsCountLabel.setText(resultsListView.getItems().size() + " photo(s)");
    }
}
