package PrimeNet;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;


public class Film {

    private SimpleBooleanProperty favourite;
    private SimpleStringProperty title;
    private SimpleIntegerProperty year;
    private SimpleStringProperty overview;
    private SimpleBooleanProperty remember;
    private SimpleStringProperty rate;
    private Image poster;
    private SimpleStringProperty categories;
    private SimpleStringProperty timeAndDate;

    public Film(boolean favourite, String title, int year, String overview, boolean remember, String rate, Image poster, String categories) {
        this.favourite = new SimpleBooleanProperty(favourite);
        this.title = new SimpleStringProperty(title);
        this.year = new SimpleIntegerProperty(year);
        this.overview = new SimpleStringProperty(overview);
        this.remember = new SimpleBooleanProperty(remember);
        this.rate = new SimpleStringProperty(rate);
        this.poster = poster;
        this.categories = new SimpleStringProperty(categories);
    }

    //this constructor will only be used for ControllerForFavourite and ControllerForBookmarks
    public Film(String title, int year, String rate) {
        this.title = new SimpleStringProperty(title);
        this.year = new SimpleIntegerProperty(year);
        this.favourite = new SimpleBooleanProperty(false);
        this.rate = new SimpleStringProperty(rate);
    }

    //this constructor will only be used for ControllerForSearchHistory
    public Film(String title, String timeAndDate) {
        this.title = new SimpleStringProperty(title);
        this.timeAndDate = new SimpleStringProperty(timeAndDate);
    }

    public String getTimeAndDate() {
        return timeAndDate.get();
    }

    public SimpleStringProperty timeAndDateProperty() {
        return timeAndDate;
    }

    public void setTimeAndDate(String timeAndDate) {
        this.timeAndDate.set(timeAndDate);
    }

    public Film(String title, int year) {
        this(title, year, " ");
    }

    public Image getPoster() {
        return poster;
    }

    public boolean getRemember() {
        return remember.get();
    }

    public void setRemember(boolean remember) {
        this.remember.set(remember);
    }

    public SimpleBooleanProperty rememberProperty() {
        return remember;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public SimpleIntegerProperty yearProperty() {
        return year;
    }

    public String getOverview() {
        return overview.get();
    }

    public void setOverview(String overview) {
        this.overview.set(overview);
    }

    public SimpleStringProperty overviewProperty() {
        return overview;
    }

    public boolean isFavourite() {
        return favourite.get();
    }

    public void setFavourite(boolean favourite) {
        this.favourite.set(favourite);
    }

    public SimpleBooleanProperty favouriteProperty() {
        return favourite;
    }

    public String getRate() {
        return rate.get();
    }

    public void setRate(String rate) {
        this.rate.set(rate);
    }

    public SimpleStringProperty rateProperty() {
        return rate;
    }

    public String getCategories() {
        return categories.get();
    }

    public SimpleStringProperty categoriesProperty() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories.set(categories);
    }
}
