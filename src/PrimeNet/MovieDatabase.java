package PrimeNet;

import PrimeNet.movies.Movie;
import PrimeNet.movies.Results;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.image.Image;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;


public class MovieDatabase {

    private static final Gson GSON = new GsonBuilder()
            .create();
    private static final String HOST = "api.themoviedb.org";
    private static final String PATH_PREFIX = "3/search/movie";
    private static final String POSTER_PREFIX = "3/movie/";
    private static String apiKey;

    public static void setApiKey(String apiKey) {
        MovieDatabase.apiKey = apiKey;
    }

    public static Results getMoviesByName(String query) {
        query = query.trim();
        if (query.isEmpty()) {
            return getEmpty();
        }

        String queryString = "api_key=" + apiKey + "&query=" + query
                + "&page=" + "1" + "&include_adult=" + "false";

        HttpsURLConnection conn = null;
        try {
            URL url = new URL("https://" + HOST + "/" + PATH_PREFIX + "?" + queryString);
            conn = (HttpsURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return GSON.fromJson(reader, Results.class);
        } catch (IOException ioException) {
            if (conn != null) {
                try {
                    if (conn.getResponseCode() == 422) {
                        return getEmpty();
                    }
                } catch (IOException ioException2) {
                }
            }

            ioException.printStackTrace(System.err);
            System.err.println("An error occurred while requesting data from API.");
        }

        return getEmpty();
    }

    public static Image getPoster(Movie movie) {
        String poster = movie.getPosterPath();
        if (poster == null || (poster = poster.trim()).isEmpty()) {
            return null;
        }

        try {
            URL url = new URL("https://image.tmdb.org/t/p/w342/" + movie.getPosterPath());
            try (InputStream in = url.openStream()) {
                return new Image(in);
            }
        } catch (IOException ioException) {
            return null;
        }
    }

    public static Results getEmpty() {
        return new Results(new ArrayList<>());
    }
}
