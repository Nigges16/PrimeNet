package PrimeNet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.NoSuchElementException;

public class ControllerForFavourite {
    @FXML
    TableView<Film> favouriteTable = new TableView();
    @FXML
    TableColumn<Film, String> favouriteTitleColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, Integer> favouriteYearColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, String> favouriteRateColumn = new TableColumn<>();
    @FXML
    ComboBox<String> favouriteLikeDislikeComboBox;

    private ObservableList<String> favouriteRate = FXCollections.observableArrayList();
    private ObservableList<Film> allFilmsInFavourite = FXCollections.observableArrayList();

    private boolean doubleClick = false;

    @FXML
    private void initialize() {
        readLinesFromFavourite(Controller.getUsername() + "/Favoriten.txt");
        setUpTableForFavourite();
        setUpFavouriteLikeDislikeComboBox();
        closingFavouriteWindowAction(Controller.getFavouriteWindow());
    }

    public void setUpFavouriteLikeDislikeComboBox() {
        favouriteLikeDislikeComboBox.getItems().addAll("Alle", "Like", "Dislike", "Unbewertet");
    }

    private void setUpTableForFavourite() {
        favouriteTable.setEditable(true);

        //favouriteTitleColumn
        favouriteTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //favouriteYearColumn
        favouriteYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        //favouriteRateColumn
        favouriteRateColumn.setEditable(true);
        favouriteRate.addAll("Like", "Dislike");
        favouriteRateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        favouriteRateColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), favouriteRate));
        favouriteRateColumn.setStyle("-fx-alignment: CENTER;");

        favouriteTable.setItems(allFilmsInFavourite);

        //if you do a double click the selected film will be shown in the main window
        favouriteTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        doubleClick = true;
                        Controller.getFavouriteWindow().close();
                        try {
                            Controller.setDoubleClickInFavouriteOrBookmarksWindow(
                                    favouriteTable.getSelectionModel().getSelectedItem().getTitle(),
                                    favouriteTable.getSelectionModel().getSelectedItem().getYear());
                            //sometimes if you do not accurately click a row, a NullPointerException will occur.
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void readLinesFromFavourite(String fileName) {
        HelperMethods.readFilmFromFile(fileName, allFilmsInFavourite);
    }

    //Before finally closing the window
    //Favourite.txt will be changed according to latest ObservableList allFilmsInFavourite
    private void closingFavouriteWindowAction(Stage stage) {
        stage.setOnHiding(event -> {
            overwriteFavourite();
            if (!doubleClick) {
                Controller.setWindowCloseAction();
            }
        });
    }

    private void overwriteFavourite() {
        HelperMethods.overwriteFileWithFilm(Controller.getUsername() + "/Favoriten.txt", allFilmsInFavourite);
    }

    //Action by pressing the delete button in favourite table
    public void deleteFilmInFavourite() {
        try {
            ObservableList<Film> productSelected;
            productSelected = favouriteTable.getSelectionModel().getSelectedItems();
            productSelected.forEach(allFilmsInFavourite::remove);
        } catch (NoSuchElementException e) {
            Controller.getFavouriteWindow().close();
        }
    }

    public void clickFavouriteLikeDislikeComboBox() {
        HelperMethods.simpleFilter(favouriteLikeDislikeComboBox.getValue(), favouriteTable, allFilmsInFavourite);
    }
}