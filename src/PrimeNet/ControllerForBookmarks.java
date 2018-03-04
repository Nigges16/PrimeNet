package PrimeNet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ControllerForBookmarks {
    @FXML
    TableView<Film> bookmarksTable = new TableView();
    @FXML
    TableColumn<Film, String> bookmarksTitleColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, Integer> bookmarksYearColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, String> bookmarksRateColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, Boolean> bookmarksFavouriteColumn = new TableColumn<>();
    @FXML
    ComboBox<String> bookmarksLikeDislikeComboBox;

    private ObservableList<String> bookmarksRate = FXCollections.observableArrayList();

    private ObservableList<Film> allFilmsInBookmarks = FXCollections.observableArrayList();

    private boolean doubleClick = false;

    @FXML
    public void initialize() {
        setUpTableForBookmarks();
        setUpBookmarksLikeDislikeComboBox();
        readLinesFromBookmarks(Controller.getUsername() + "/Bookmarks.txt");
        setUpFavouriteColumnInBookmarks();
        closingBookmarksWindowAction(Controller.getBookmarksWindow());
    }

    private void setUpBookmarksLikeDislikeComboBox() {
        bookmarksLikeDislikeComboBox.getItems().addAll("Alle", "Like", "Dislike", "Unbewertet");
    }

    private void setUpFavouriteColumnInBookmarks() {
        for (Film filmInBookmarks : allFilmsInBookmarks) {
            filmInBookmarks.favouriteProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue && !oldValue) {
                    writeInFavouriteFile(filmInBookmarks.getTitle(), String.valueOf(filmInBookmarks.getYear()),
                            filmInBookmarks.getRate());
                    try {
                        List<Film> productSelected = new ArrayList<>();
                        productSelected.add(filmInBookmarks);
                        productSelected.forEach(allFilmsInBookmarks::remove);
                    } catch (NoSuchElementException e) {
                        Controller.getBookmarksWindow().close();
                    }
                }
            });
        }
    }

    private void writeInFavouriteFile(String filmTitle, String filmYear, String filmRate) {
        HelperMethods.writeFilmInFile(Controller.getUsername() + "/Favoriten.txt", filmTitle, filmYear, filmRate);
    }


    private void setUpTableForBookmarks() {
        bookmarksTable.setEditable(true);

        //bookmarksTitleColumn
        bookmarksTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //bookmarksYearColumn
        bookmarksYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        //bookmarksRateColumn
        bookmarksRateColumn.setEditable(true);
        bookmarksRate.addAll("Like", "Dislike");
        bookmarksRateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        bookmarksRateColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), bookmarksRate));
        bookmarksRateColumn.setStyle("-fx-alignment: CENTER;");

        //bookmarksFavouriteColumn
        bookmarksFavouriteColumn.setEditable(true);
        bookmarksFavouriteColumn.setCellValueFactory(new PropertyValueFactory<>("favourite"));
        bookmarksFavouriteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(bookmarksFavouriteColumn));

        bookmarksTable.setItems(allFilmsInBookmarks);

        //if you do a double click the selected film will be shown in the main window
        bookmarksTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        doubleClick = true;
                        Controller.getBookmarksWindow().close();
                        try {
                            Controller.setDoubleClickInFavouriteOrBookmarksWindow(
                                    bookmarksTable.getSelectionModel().getSelectedItem().getTitle(),
                                    bookmarksTable.getSelectionModel().getSelectedItem().getYear());
                            //sometimes if you do not accurately click a row, a NullPointerException will occur.
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    private void readLinesFromBookmarks(String fileName) {
        HelperMethods.readFilmFromFile(fileName, allFilmsInBookmarks);
    }

    //Bookmarks.txt will be changed acoording to latest ObservableList allFilmsInBookmarks
    private void closingBookmarksWindowAction(Stage stage) {
        stage.setOnHiding(event -> {
            overwriteInBookmarks();
            if (!doubleClick)
                Controller.setWindowCloseAction();
        });
    }

    private void overwriteInBookmarks() {
        HelperMethods.overwriteFileWithFilm(
                Controller.getUsername() + "/Bookmarks.txt", allFilmsInBookmarks);
    }

    //Action by pressing the delete button in bookmarks table
    public void deleteFilmInBookmarks() {
        try {
            ObservableList<Film> productSelected;
            productSelected = bookmarksTable.getSelectionModel().getSelectedItems();
            productSelected.forEach(allFilmsInBookmarks::remove);
        } catch (NoSuchElementException e) {
            Controller.getBookmarksWindow().close();
        }
    }

    public void clickBookmarksLikeDislikeComboBox() {
        HelperMethods.simpleFilter(bookmarksLikeDislikeComboBox.getValue(), bookmarksTable, allFilmsInBookmarks);
    }
}
