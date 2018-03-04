package PrimeNet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;


public class ControllerForSearchHistory {

    @FXML
    TableView<Film> searchHistoryTable = new TableView<>();
    @FXML
    TableColumn<Film, String> searchKeywordColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, String> searchTimeColumn = new TableColumn<>();

    private boolean doubleClick = false;
    private ObservableList<Film> keywordAndTimeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        HelperMethods.readFilmAndDateFromFile(Controller.getUsername() + "/SearchHistory.txt", keywordAndTimeList);
        setUpSearchHistoryTable();
    }

    private void setUpSearchHistoryTable() {
        //keywordColumn
        searchKeywordColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //TimeColumn
        searchTimeColumn.setCellValueFactory(new PropertyValueFactory<>("timeAndDate"));

        searchHistoryTable.setItems(keywordAndTimeList);

        //if you do a double click the selected film will be shown in the main window
        searchHistoryTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        doubleClick = true;
                        try {
                            Controller.setDoubleClickInSearchHistory(
                                    searchHistoryTable.getSelectionModel().getSelectedItem().getTitle());
                            //sometimes if you do not accurately click a row, a NullPointerException will occur.
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        Controller.getSearchHistoryWindow().close();
                    }
                }
            }
        });
    }

    public void clickDeleteHistory() {
        ObservableList<Film> emptyList = FXCollections.observableArrayList();
        keywordAndTimeList = emptyList;
        searchHistoryTable.setItems(keywordAndTimeList);
        File copy = new File(Controller.getUsername() + "/copyOfSearchHistory.txt");
        File original = new File(Controller.getUsername() + "/SearchHistory.txt");
        HelperMethods.overwriteSecondFileWithFirstFile(copy, original);
    }
}
