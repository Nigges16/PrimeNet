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
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;

public class ControllerForFavourite {
    @FXML
    TableView<Film> favouriteTable = new TableView();
    @FXML
    TableColumn<Film, String> favouriteTitleColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, Integer> favouriteYearColumn = new TableColumn<>();
    @FXML
    TableView<Film> bookmarksTable = new TableView();
    @FXML
    TableColumn<Film, String> bookmarksTitleColumn = new TableColumn<>();
    @FXML
    TableColumn<Film, Integer> bookmarksYearColumn = new TableColumn<>();

    ObservableList<Film> allFilmsInFavourite = FXCollections.observableArrayList();

    ObservableList<Film> allFilmsInBookmarks = FXCollections.observableArrayList();

    File file;
    FileWriter writer;

    @FXML
    private void initialize() {
        setUpTable();
        readLinesFromFavourite("Favoriten.txt");
        closingFavouriteWindowAction(Controller.getFavouriteWindow());

        setUpTableForBookmarks();
        readLinesFromBookmarks("Bookmarks.txt");
        closingBookmarksWindowAction(Controller.getBookmarksWindow());
    }

    public void setUpTable() {
        //favouriteTitleColumn
        favouriteTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //favouriteYearColumn
        favouriteYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        favouriteTable.setItems(allFilmsInFavourite);

        //if you do a double click the selected film will be shown in the main window
        favouriteTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        System.out.println("Double click");
                    }
                }
            }
        });
    }

    public void setUpTableForBookmarks(){
        //bookmarksTitleColumn
        bookmarksTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //favouriteYearColumn
        bookmarksYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        bookmarksTable.setItems(allFilmsInBookmarks);

        //if you do a double click the selected film will be shown in the main window
        bookmarksTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        System.out.println("Double click");
                    }
                }
            }
        });
    }

    public void readLinesFromFavourite(String fileName){
        readLinesFromFile(fileName, allFilmsInFavourite);
    }

    public void readLinesFromBookmarks(String fileName){
        readLinesFromFile(fileName, allFilmsInBookmarks);
    }
    private void readLinesFromFile(String datName, ObservableList<Film> allFilms) {
        File file = new File(datName);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(datName));
            String line = null;
            String[] word;
            //read lines by lines and add the films to favouriteTableView
            while ((line = in.readLine()) != null) {
                //Strings in these lines are separated by a tab, we will get each of them and create a instance of film
                //then we will add it to a new list which we will later use to generate our list in favouriteTableView
                word = line.split("\t");
                allFilms.add(makeFilm(word[0], Integer.parseInt(word[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Film makeFilm(String title, int year) {
        Film film = new Film(title, year);
        return film;
    }

    //Before finally closing the window
    //Favourite.txt will be changed according to latest ObservableList allFilmsInFavourite
    public void closingFavouriteWindowAction(Stage stage) {
        stage.setOnHiding(event -> writeInFavourite());
    }

    //Bookmarks.txt will be changed acoording to latest ObservableList allFilmsInBookmarks
    public void closingBookmarksWindowAction(Stage stage) {
        stage.setOnHiding(event -> writeInBookmarks());
    }

    //Action by pressing the delete button in favourite table
    public void deleteFilmInFavourite() {
        try{
            ObservableList<Film> productSelected;
            productSelected = favouriteTable.getSelectionModel().getSelectedItems();
            productSelected.forEach(allFilmsInFavourite::remove);
        } catch (NoSuchElementException e){Controller.getFavouriteWindow().close();}
    }

    //Action by pressing the delete button in bookmarks table
    public void deleteFilmInBookmarks() {
        try{
            ObservableList<Film> productSelected;
            productSelected = bookmarksTable.getSelectionModel().getSelectedItems();
            productSelected.forEach(allFilmsInBookmarks::remove);
        } catch (NoSuchElementException e){Controller.getBookmarksWindow().close();}
    }

    public void writeInFavourite() {
        overwriteFile("Favoriten.txt", allFilmsInFavourite);
    }

    public void writeInBookmarks(){
        overwriteFile("Bookmarks.txt", allFilmsInBookmarks);
    }

    public void overwriteFile(String pathname, ObservableList<Film> allFilms){
        file = new File(pathname);
        try {
            writer = new FileWriter(file);
            for (Film f : allFilms) {
                writer.write(f.getTitle());
                writer.write("\t");
                writer.write(String.valueOf(f.getYear()));
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    //
    //
    //


}

