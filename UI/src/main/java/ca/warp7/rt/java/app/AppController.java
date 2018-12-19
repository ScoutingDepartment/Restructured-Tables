package ca.warp7.rt.java.app;

import ca.warp7.rt.java.core.ft.Feature;
import ca.warp7.rt.java.core.ft.FeatureAction;
import ca.warp7.rt.java.core.ft.FeatureIcon;
import ca.warp7.rt.java.core.ft.FeatureStage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static ca.warp7.rt.java.app.AppFeatures.featureMap;
import static ca.warp7.rt.java.app.AppFeatures.features;

public class AppController implements FeatureStage {

    private static final Paint gray = Paint.valueOf("gray");
    private static final Paint white = Paint.valueOf("white");

    @FXML
    MenuButton newButton;
    @FXML
    BorderPane tabContent;
    @FXML
    HBox tabsAndContent;
    @FXML
    ListView<AppActionTab> appActionListView;
    @FXML
    VBox listViewContainer;

    private ObservableList<AppActionTab> appActions = FXCollections.observableArrayList();
    private Feature currentFeature = null;
    private Map<String, ArrayList<FeatureAction>> tabGroups = new LinkedHashMap<>();
    private Stage appStage;
    private BooleanProperty hideSidebar = new SimpleBooleanProperty();

    public void toggleFullScreen() {
        appStage.setFullScreen(!appStage.isFullScreen());
    }

    public void toggleSidebar() {
        hideSidebar.setValue(!hideSidebar.get());
    }

    @Override
    public void setStage(Stage stage) {
        stage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F11) stage.setFullScreen(true);
            else if (event.getCode() == KeyCode.F9) toggleSidebar();
        });
        stage.setOnCloseRequest(event -> {
            if (currentFeature != null && !currentFeature.onCloseRequest()) {
                event.consume();
            }
        });
        appStage = stage;
        appStage.setMinWidth(800);
        appStage.setMinHeight(450);
        appStage.setMaximized(true);
    }

    @FXML
    void initialize() {
        appActionListView.setItems(appActions);
        appActionListView.setCellFactory(listView -> new ListCell<AppActionTab>() {
            @Override
            protected void updateItem(AppActionTab item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                if (item.isSeparator()) {
                    setMouseTransparent(true);
                    setFocusTraversable(false);
                } else {
                    setGraphic(AppElement.tabUIFromAction(item));
                    setOnMouseClicked(event -> handleFeatureAction(item.getFeatureAction()));
                }
            }
        });
        features.forEach(this::initFeature);
        tabGroups.forEach((s, featureActions) -> {
            appActions.add(AppActionTab.separator);
            featureActions.forEach(action -> appActions.add(new AppActionTab(action)));
        });
        appActions.add(AppActionTab.separator);
        hideSidebar.addListener((observable, oldValue, selected) -> {
            if (selected) tabsAndContent.getChildren().remove(0);
            else tabsAndContent.getChildren().add(0, listViewContainer);
        });
        appActionListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                ObservableList<AppActionTab> selectedItems = appActionListView.getSelectionModel().getSelectedItems();
                if (selectedItems.size() == 1) {
                    AppActionTab tab = selectedItems.get(0);
                    if (!tab.isSeparator()) handleFeatureAction(tab.getFeatureAction());
                }
            }
        });
        appActionListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<AppActionTab>) c -> {
            c.next();
            c.getRemoved().forEach(o -> {
                if (o.getIcon() != null) o.getIcon().setIconColor(gray);
            });
            c.getAddedSubList().forEach(o -> {
                if (o.getIcon() != null) o.getIcon().setIconColor(white);
            });
        });
    }

    private void initFeature(Feature feature) {
        feature.init();
        ObservableList<FeatureAction> actions = feature.getActionList();
        actions.forEach(action -> {
            switch (action.getType()) {
                case New:
                    MenuItem item = new MenuItem();
                    item.setText(action.getActionTitle());
                    item.setGraphic(new FeatureIcon(action.getIconLiteral()));
                    item.setOnAction(event -> handleFeatureAction(action));
                    newButton.getItems().add(item);
                    break;
                case TabItem:
                    String groupName;
                    switch (action.getActionGroup()) {
                        case Core:
                            groupName = "core";
                            break;
                        case SingleTab:
                            groupName = "single";
                            break;
                        case WithFeature:
                            groupName = action.getFeatureId();
                            break;
                        default:
                            groupName = "unknown";
                    }
                    if (!tabGroups.containsKey(groupName)) tabGroups.put(groupName, new ArrayList<>());
                    tabGroups.get(groupName).add(action);
            }
        });
    }

    @FXML
    void showMemory() {
        String mem = String.format("Memory: %.2f MB", (Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()) / 1000000.0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mem, ButtonType.OK);
        new Thread(() -> Runtime.getRuntime().gc()).start();
        alert.showAndWait();
    }

    private void handleFeatureAction(FeatureAction action) {
        String id = action.getFeatureId();
        if (featureMap.containsKey(id)) {
            Feature feature = featureMap.get(id);
            if (currentFeature == feature) currentFeature.onAction(action.getType(), action.getParamString());
            else if (currentFeature == null || currentFeature.onCloseRequest()) {
                currentFeature = feature;
                Parent parent = currentFeature.onAction(action.getType(), action.getParamString());
                tabContent.setCenter(parent);
            }
        }
    }
}
