package PrimeNet;

import PrimeNet.movies.Movie;
import PrimeNet.movies.Results;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Controller {
    //main window
    @FXML
    public ProgressIndicator progressbar;
    @FXML
    public Label StoryLabel;
    @FXML
    ComboBox<String> categoriesComboBox;
    @FXML
    TextField searchField = new TextField();
    @FXML
    TableView<Film> filmTable = new TableView<>();
    @FXML
    private TableColumn<Film, Boolean> favouriteColumn = new TableColumn<>("Favorit");
    @FXML
    private TableColumn<Film, String> titleColumn = new TableColumn<>("Filmtitel");
    @FXML
    private TableColumn<Film, Integer> yearColumn = new TableColumn<>("Jahr");
    @FXML
    private TableColumn<Film, Boolean> rememberColumn = new TableColumn<>("Merken");
    @FXML
    private TableColumn<Film, String> categoriesColumn = new TableColumn<>("Kategorien");
    @FXML
    ComboBox<String> yearComboBox = new ComboBox<>();
    @FXML
    ImageView previewPane = new ImageView();
    @FXML
    Label previewTitle = new Label();
    @FXML
    Label previewDate = new Label();
    @FXML
    private Label previewRate = new Label();
    @FXML
    Label previewOverview = new Label();

    //mostly constructors
    private ObservableList<Film> originalFilms = FXCollections.observableArrayList();
    private ObservableList<Film> originalFilmsForSecondFilterAction = FXCollections.observableArrayList();
    private static String username;
    private static String titleForSearch;
    private static int yearForSearch;
    private static SimpleBooleanProperty windowCloseAction = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty doubleClickSecondaryWindow = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty doubleClickSearchHistoryWindow = new SimpleBooleanProperty(false);
    private static Stage favouriteWindow = new Stage();
    private static Stage bookmarksWindow = new Stage();
    private static Stage searchHistoryWindow = new Stage();
    private static String filmsInFavouriteAsString;
    private static String filmsInBookmarksAsString;

    public static void setUsername(String name) {
        username = name;
    }

    public static String getUsername() {
        return username;
    }

    public static Stage getSearchHistoryWindow() {
        return searchHistoryWindow;
    }

    public static void setWindowCloseAction() {
        if (windowCloseAction.getValue())
            windowCloseAction.set(false);
        else
            windowCloseAction.set(true);
    }

    public static Stage getBookmarksWindow() {
        return bookmarksWindow;
    }

    public static Stage getFavouriteWindow() {
        return favouriteWindow;
    }

    public static void setDoubleClickInFavouriteOrBookmarksWindow(String title, int year) {
        titleForSearch = title;
        yearForSearch = year;
        if (doubleClickSecondaryWindow.getValue())
            doubleClickSecondaryWindow.set(false);
        else
            doubleClickSecondaryWindow.set(true);
    }

    public static void setDoubleClickInSearchHistory(String keyword) {
        titleForSearch = keyword;
        if (doubleClickSearchHistoryWindow.getValue())
            doubleClickSearchHistoryWindow.set(false);
        else
            doubleClickSearchHistoryWindow.set(true);
    }

    @FXML
    private void initialize() {
        setUpPreview();
        bookmarksWindow.initModality(Modality.APPLICATION_MODAL);
        favouriteWindow.initModality(Modality.APPLICATION_MODAL);
        searchHistoryWindow.initModality(Modality.APPLICATION_MODAL);
        windowCloseAction.addListener((observableValue, oldValue, newValue) -> {
            try {
                refreshFilmList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        doubleClickSecondaryWindow.addListener(((observable, oldValue, newValue) -> {
            searchForThis(titleForSearch, yearForSearch);
        }));
        doubleClickSearchHistoryWindow.addListener(((observable, oldValue, newValue) -> {
            searchForThis(titleForSearch);
        }));
        setUpTables();
        progressbar.setProgress(-1.0f);
        progressbar.setVisible(false);

        AutoCompletionBinding<String> autoCompletionBinding =
                TextFields.bindAutoCompletion(searchField, suggestionrequest -> {
                    Results movies = MovieDatabase.getMoviesByName(suggestionrequest.getUserText());
                    return movies.getMovies().stream().map(Movie::getTitle).collect(Collectors.toList());
                });

        autoCompletionBinding.setOnAutoCompleted(event -> {
            event.consume();
            searchField.setText(event.getCompletion());
            searchField.positionCaret(event.getCompletion().length());
            onEnter();
        });
    }

    private void setUpPreview() {
        previewPane.setImage(null);
        previewTitle.setText("");
        previewDate.setText("");
        previewRate.setText("");
        previewOverview.setText("");
        StoryLabel.setVisible(false);
    }

    private void refreshFilmList() {
        makeFavouriteFileToString();
        makeBookmarksFileToString();
        for (Film film : originalFilmsForSecondFilterAction) {
            if (isThisFilmInFavourite(film))
                film.setFavourite(true);
            else
                film.setFavourite(false);

            if (isThisFilmInBookmarks(film))
                film.setRemember(true);
            else
                film.setRemember(false);

        }
    }

    private boolean isThisFilmInFavourite(Film film) {
        return HelperMethods.isThisFilmInFile(film, filmsInFavouriteAsString);
    }

    private boolean isThisFilmInBookmarks(Film film) {
        return HelperMethods.isThisFilmInFile(film, filmsInBookmarksAsString);
    }

    private void searchForThis(String title, int year) {
        progressbar.setVisible(true);
        Thread t1 = new Thread(() -> {
            try {
                makeFavouriteFileToString();
                makeBookmarksFileToString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                originalFilms = getFilm(correctStringForSearch(title));
            } catch (Exception e) {
                e.printStackTrace();
            }

            progressbar.setVisible(false);
            filmTable.setItems(originalFilms);

            //search result will be filtered according to selected years
            //AND categories. This is implemented in method filterTableViewAccToYears
            filterTableViewAccToYears(String.valueOf(year));

            addListenerToCheckBoxInFavouriteColumn();
            addListenerToCheckBoxInBookmarksColumn();
        });
        t1.start();
    }

    private void searchForThis(String keyword) {
        progressbar.setVisible(true);
        Thread t1 = new Thread(() -> {
            try {
                makeFavouriteFileToString();
                makeBookmarksFileToString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                originalFilms = getFilm(correctStringForSearch(keyword));
            } catch (Exception e) {
                e.printStackTrace();
            }

            progressbar.setVisible(false);
            filmTable.setItems(originalFilms);
            addListenerToCheckBoxInFavouriteColumn();
            addListenerToCheckBoxInBookmarksColumn();
            searchField.setText(keyword);
        });
        t1.start();
    }

    private void setUpTables() {
        fillCategoriesComboBox();
        fillYearComboBox();
        setUpTableOfFilm();
    }

    private void fillCategoriesComboBox() {
        categoriesComboBox.getItems().add("Alle");
        categoriesComboBox.getItems().addAll("Abenteuer", "Action", "Animation", "Dokumentarfilm",
                "Drama", "Familie", "Fantasy", "Historie", "Horror", "Komödie", "Kriegsfilm", "Krimi",
                "Liebesfilm", "Musik", "Mystery", "Science", "Fiction", "TV-Film", "Thriller", "Western");
    }

    private void fillYearComboBox() {
        yearComboBox.getItems().add("Alle");
        for (int n = 2018; n >= 1950; n--) {
            yearComboBox.getItems().add(String.valueOf(n));
        }
    }

    private void setUpTableOfFilm() {
        filmTable.setEditable(true);

        //titleColumn
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(400);

        //yearColumn
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setMaxWidth(70);
        yearColumn.setMinWidth(70);
        yearColumn.setPrefWidth(70);
        yearColumn.setStyle("-fx-alignment: CENTER;");

        //rememberColumn
        rememberColumn.setEditable(true);
        rememberColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        rememberColumn.setCellValueFactory(new PropertyValueFactory<>("remember"));
        rememberColumn.setCellFactory(CheckBoxTableCell.forTableColumn(rememberColumn));
        rememberColumn.setMaxWidth(70);
        rememberColumn.setMinWidth(70);

        //favouriteColumn
        favouriteColumn.setEditable(true);
        favouriteColumn.setMaxWidth(60);
        favouriteColumn.setMinWidth(60);
        favouriteColumn.setCellValueFactory(new PropertyValueFactory<>("favourite"));
        favouriteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(favouriteColumn));

        //categoriesColumn
        categoriesColumn.setCellValueFactory(new PropertyValueFactory<>("categories"));
        categoriesColumn.setMaxWidth(300);
        categoriesColumn.setMinWidth(300);

        filmTable.getColumns().addAll(favouriteColumn, titleColumn, categoriesColumn, yearColumn, rememberColumn);
    }

    public void filmTableIsClicked() {
        Film film = filmTable.getSelectionModel().getSelectedItem();
        if (film == null) {
            previewPane.setImage(null);
            previewTitle.setText("");
            previewDate.setText("");
            previewRate.setText("");
            previewOverview.setText("");
            StoryLabel.setVisible(false);
            return;
        }
        previewPane.setImage(film.getPoster());
        previewTitle.setText(film.getTitle());
        String year = Integer.toString(film.getYear());
        previewDate.setText("(" + year + ")");
        previewRate.setText(film.getRate());
        previewOverview.setText(film.getOverview());
    }

    //by clicking the button "Chronik" the latest searchHistory will be opened
    public void clickSearchHistory() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("FxmlFiles/SearchHistory.fxml"));
        HelperMethods.openNewWindow(searchHistoryWindow, "Chronik", root);
    }

    //by clicking the button "Favoriten" favourite window will be opened
    public void clickFavourite() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("FxmlFiles/Favourite.fxml"));
        HelperMethods.openNewWindow(favouriteWindow, "Favoriten", root);
    }

    //by clicking the button "Merkliste" bookmarks window will be opened
    public void clickBookmarks() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("FxmlFiles/Bookmarks.fxml"));
        HelperMethods.openNewWindow(bookmarksWindow, "Merkliste", root);
    }

    //if enter is pressed table of film will be filled with new content
    public void onEnter() {
        progressbar.setVisible(true);
        new Thread(() -> {
            try {
                makeFavouriteFileToString();
                makeBookmarksFileToString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                originalFilms = getFilm(correctStringForSearch(searchField.getText()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            progressbar.setVisible(false);
            filmTable.setItems(originalFilms);

            //search result will be filtered according to selected years
            //AND categories. This is implemented in method filterTableViewAccToYears
            filterTableViewAccToYears(yearComboBox.getValue());

            addListenerToCheckBoxInFavouriteColumn();
            addListenerToCheckBoxInBookmarksColumn();
            writeKeywordAndTimeInFile(searchField.getText());
        }).start();

    }

    //making searchHistory
    private void writeKeywordAndTimeInFile(String keyword) {
        Calendar cal = Calendar.getInstance();
        String date = String.valueOf(cal.get(Calendar.YEAR)) + "." + String.valueOf(cal.get(Calendar.MONTH) + 1)
                + "." + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String timeOfDay = String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
                cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
        String keywordAndDate = keyword + "\t" + date + " " + timeOfDay;
        HelperMethods.writeTextInFile(username + "/SearchHistory.txt", keywordAndDate);
    }

    private void makeFavouriteFileToString() {
        try {
            File fileFavourite = new File(username + "/Favoriten.txt");
            filmsInFavouriteAsString = HelperMethods.makeFileToString(fileFavourite);
        } catch (NullPointerException e) {
            filmsInFavouriteAsString = "";
        }
    }

    private void makeBookmarksFileToString() {
        try {
            File fileBookmarks = new File(username + "/Bookmarks.txt");
            filmsInBookmarksAsString = HelperMethods.makeFileToString(fileBookmarks);
        } catch (NullPointerException e) {
            filmsInBookmarksAsString = "";
        }
    }

    private String correctStringForSearch(String keyword) {
        String pattern = "\\s+";
        return keyword.replaceAll(pattern, "+");
    }

    //getting all films into the filmTabel
    private ObservableList<Film> getFilm(String searchCorrection) {
        ObservableList<Film> films = FXCollections.observableArrayList();
        Results r = MovieDatabase.getMoviesByName(searchCorrection);
        r.getMovies()
                .stream()
                .map(movie ->
                        new Film(isThisMovieInFavourite(movie), movie.getTitle(), movie.getReleaseYear(),
                                movie.getOverview(), isThisMovieInBookmarks(movie), "", MovieDatabase.getPoster(movie),
                                movie.getCategories())
                )
                .forEach(films::add);
        return films;
    }

    //if favourite Checkbox is clicked the film will be written in a File
    private void addListenerToCheckBoxInFavouriteColumn() {
        for (Film film : originalFilms) {
            film.favouriteProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue)
                    writeInFavourite(film.getTitle(), String.valueOf(film.getYear()), film.getRate());
                else if (!newValue)
                    deleteInFavourite(film.getTitle(), String.valueOf(film.getYear()));
            });
        }
    }

    //if remember Checkbox is clicked the film will be written in a File
    private void addListenerToCheckBoxInBookmarksColumn() {
        for (Film film : originalFilms) {
            film.rememberProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue)
                    writeInBookmarks(film.getTitle(), String.valueOf(film.getYear()), film.getRate());
                else if (!newValue)
                    deleteInBookmarks(film.getTitle(), String.valueOf(film.getYear()));
            });
        }
    }

    //write in Favoriten.txt to save checkbox action from favouriteColumn
    private void writeInFavourite(String filmTitle, String filmYear, String filmRate) {
        HelperMethods.writeFilmInFile(username + "/Favoriten.txt", filmTitle, filmYear, filmRate);
    }

    //write in Bookmarks.txt to save checkbox action from rememberColumn
    private void writeInBookmarks(String filmTitle, String filmYear, String filmRate) {
        HelperMethods.writeFilmInFile(username + "/Bookmarks.txt", filmTitle, filmYear, filmRate);
    }

    private void deleteInFavourite(String title, String year) {
        File original = new File(username + "/Favoriten.txt");
        File copy = new File(username + "/copyOfFavourite.txt");
        HelperMethods.copyOriginalFileBesidesOneLine(original, copy, title, year);
        //At first we wanted to delete the original file and then rename the copy file, however the method delete()
        //does not work because of unknown reason. So this is our second best solution to resolve this issue.
        HelperMethods.overwriteSecondFileWithFirstFile(copy, original);
    }

    private void deleteInBookmarks(String title, String year) {
        File original = new File(username + "/Bookmarks.txt");
        File copy = new File(username + "/copyOfBookmarks.txt");
        HelperMethods.copyOriginalFileBesidesOneLine(original, copy, title, year);
        HelperMethods.overwriteSecondFileWithFirstFile(copy, original);
    }

    private boolean isThisMovieInFavourite(Movie movie) {
        String findMovieKeyword = movie.getTitle() + "\t" + movie.getReleaseYear();
        if (filmsInFavouriteAsString == null) {
            filmsInFavouriteAsString = "";
        }
        Matcher matcher = Pattern.compile(findMovieKeyword).matcher(filmsInFavouriteAsString);
        return matcher.find();
    }

    private boolean isThisMovieInBookmarks(Movie movie) {
        String findMovieKeyword = movie.getTitle() + "\t" + movie.getReleaseYear();
        if (filmsInBookmarksAsString == null) {
            filmsInBookmarksAsString = "";
        }
        Matcher matcher = Pattern.compile(findMovieKeyword).matcher(filmsInBookmarksAsString);
        return matcher.find();
    }

    public void clickYearComboBox() {
        filterTableViewAccToYears(yearComboBox.getValue());
    }

    public void clickCategoriesComboBox() {
        filterTableViewAccToYears(yearComboBox.getValue());
    }

    //filter tableView list according to selected year
    private void filterTableViewAccToYears(String yearOfComboBox) {
        ObservableList<Film> selectedYearFilms;
        selectedYearFilms = FXCollections.observableArrayList();
        try {
            int year = Integer.parseInt(yearOfComboBox);
            for (Film s : originalFilms) {
                if (s.getYear() == year) {
                    selectedYearFilms.add(s);
                }
            }
        } catch (NumberFormatException e) {
            selectedYearFilms = originalFilms;
        }
        originalFilmsForSecondFilterAction = selectedYearFilms;
        filterTableViewAccToCategories(categoriesComboBox.getValue());
        filmTable.setItems(originalFilmsForSecondFilterAction);
    }

    //filter tableView list according to selected category
    private void filterTableViewAccToCategories(String categoryOfComboBox) {
        ObservableList<Film> selectedCategoriesFilms;
        selectedCategoriesFilms = FXCollections.observableArrayList();
        if (categoryOfComboBox == null) {
        } else if (categoryOfComboBox.equals("Alle")) {
        } else {
            for (Film s : originalFilmsForSecondFilterAction) {
                Matcher matcher = Pattern.compile(categoryOfComboBox).matcher(s.getCategories());
                if (matcher.find())
                    selectedCategoriesFilms.add(s);
            }
            originalFilmsForSecondFilterAction = selectedCategoriesFilms;
        }
    }
}