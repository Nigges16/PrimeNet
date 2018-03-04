package PrimeNet;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class HelperMethods {

    public static boolean isThisFilmInFile(Film film, String stringOfFile) {
        String findMovieKeyword = film.getTitle() + "\t" + String.valueOf(film.getYear());
        if (stringOfFile == null) {
            stringOfFile = "";
        }
        Matcher matcher = Pattern.compile(findMovieKeyword).matcher(stringOfFile);
        return matcher.find();
    }

    //search for the line which contains the film
    private static int filmIsInLineNumber(File file, String title, String year) {
        String stringOfFile = makeFileToString(file);
        if (isThisFilmInFile(new Film(title, Integer.parseInt(year)), stringOfFile)) {
            BufferedReader br;
            String line;
            int stringInLineNumber = 0;
            try {
                br = new BufferedReader(new FileReader(file));
                int counter = 0;
                while ((line = br.readLine()) != null) {
                    if (line.equals(title + "\t" + year)) {
                        stringInLineNumber = counter;
                        break;
                    }
                    counter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringInLineNumber;
        } else
            return -1;
    }

    //first the file will be checked whether it has the film already inside
    //if so nothing happens
    public static void writeFilmInFile(String pathname, String filmTitle, String filmYear, String filmRate) {
        File file = new File(pathname);
        FileWriter writer;
        String stringOfFile = makeFileToString(file);
        if (!isThisFilmInFile(new Film(filmTitle, Integer.parseInt(filmYear)), stringOfFile)) {
            try {
                writer = new FileWriter(file, true);
                writer.write(filmTitle);
                writer.write("\t");
                writer.write(filmYear);
                writer.write("\t");
                writer.write(filmRate);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeTextInFile(String pathname, String line) {
        try {
            Files.write(Paths.get(pathname), String.format("%s\n", line).getBytes());
        } catch (IOException ioExeption) {
            ioExeption.printStackTrace(System.err);
        }
    }

    public static void copyOriginalFileBesidesOneLine(File original, File copy, String title, String year) {
        int counter = 0;
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(original)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(copy)));
            int stringInLine = HelperMethods.filmIsInLineNumber(original, title, year);
            if (stringInLine == -1) {
                try {
                    while ((line = br.readLine()) != null) {
                        bw.write(line);
                        bw.write(System.getProperty("line.separator"));
                    }
                    bw.close();
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    while ((line = br.readLine()) != null) {
                        if (counter != stringInLine) {
                            bw.write(line);
                            bw.write(System.getProperty("line.separator"));
                        }
                        counter++;
                    }
                    bw.close();
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void overwriteSecondFileWithFirstFile(File sourceFile, File overwrittenFile) {
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(overwrittenFile)));
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.write(System.getProperty("line.separator"));
            }
            br.close();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openNewWindow(Stage stage, String title, Parent root) {
        try {
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IllegalStateException e) {
            stage.show();
        }
    }

    public static String makeFileToString(File file) {
        String fileString = "";
        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((line = br.readLine()) != null) {
                fileString += line;
                fileString += "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileString;
    }

    public static void readFilmFromFile(String datName, ObservableList<Film> allFilms) {
        File file = new File(datName);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(datName));
            String line;
            String[] word;
            //read lines by lines and add the films to TableView
            while ((line = in.readLine()) != null) {
                //Strings in these lines are separated by a tab, we will get each of them and create a instance of film
                //then we will add it to a new list which we will later use to generate our list in TableView
                word = line.split("\t");
                try {
                    allFilms.add(makeFilm(word[0], Integer.parseInt(word[1]), word[2]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    allFilms.add(makeFilm(word[0], Integer.parseInt(word[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void readFilmAndDateFromFile(String pathname, ObservableList<Film> keywordAndTimeList) {
        File file = new File(pathname);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(pathname));
            String line;
            String[] word;
            //read lines by lines and add the films to TableView
            while ((line = in.readLine()) != null) {
                //Strings in these lines are separated by a tab, we will get each of them and create a instance of film
                //then we will add it to a new list which we will later use to generate our list in TableView
                word = line.split("\t");
                try {
                    keywordAndTimeList.add(makeFilmWithTime(word[0], word[1]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    keywordAndTimeList.add(makeFilmWithTime(word[0], word[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Film makeFilmWithTime(String keyword, String timeAndDate) {
        return new Film(keyword, timeAndDate);
    }

    private static Film makeFilm(String title, int year, String filmRate) {
        return new Film(title, year, filmRate);
    }

    private static Film makeFilm(String title, int year) {
        return new Film(title, year);
    }

    public static void overwriteFileWithFilm(String pathname, ObservableList<Film> allFilms) {
        File file = new File(pathname);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            for (Film f : allFilms) {
                writer.write(f.getTitle());
                writer.write("\t");
                writer.write(String.valueOf(f.getYear()));
                writer.write("\t");
                writer.write(f.getRate());
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printFile(String pathname) {
        File file = new File(pathname);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(pathname));
            String line;
            //read lines by lines and add the films to favouriteTableView
            while ((line = in.readLine()) != null) {
                //Strings in these lines are separated by a tab, we will get each of them and create a instance of film
                //then we will add it to a new list which we will later use to generate our list in favouriteTableView
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void simpleFilter(String comboBoxValue, TableView<Film> table, ObservableList<Film> filmList) {
        String keyword = comboBoxValue;
        ObservableList<Film> selectedFilms;
        selectedFilms = FXCollections.observableArrayList();
        if (keyword == null) {
        } else if (keyword.equals("Alle")) {
            selectedFilms = filmList;
        } else if (keyword.equals("Unbewertet")) {
            for (Film s : filmList) {
                Matcher matcher = Pattern.compile(" ").matcher(s.getRate());
                if (matcher.find())
                    selectedFilms.add(s);
            }
        } else {
            for (Film s : filmList) {
                Matcher matcher = Pattern.compile(keyword).matcher(s.getRate());
                if (matcher.find())
                    selectedFilms.add(s);
            }
        }
        table.setItems(selectedFilms);
    }

    public static void createAFile(String filename) {
        try {
            FileWriter writer = new FileWriter(filename, true);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
