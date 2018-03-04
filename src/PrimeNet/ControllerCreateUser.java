package PrimeNet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ControllerCreateUser {
    @FXML
    public Button CreateNewUser;

    @FXML
    public TextField CreateUserUsername;

    @FXML
    public TextField CreateUserAPIkey;

    @FXML
    public PasswordField CreateUserPassword;

    PasswordAuthentication auth = new PasswordAuthentication(18);

    //creating a new user with his own password
    @FXML
    private void CreateNewUser(ActionEvent e) throws IOException {
        // this checks if username is correct
        if (CreateUserUsername.getText().length() < 3){
            CreateUserUsername.setPromptText("mind. 3 Zeichen bitte");
        }
        else{
            List<String> lines = new ArrayList<>();
            try {
                lines = Files.readAllLines(Paths.get("passwords.txt"));
            } catch (IOException e1) {
            }

            boolean written = false;
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("passwords.txt"))) {
                for (String line : lines) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split(":");
                    if (parts.length != 3) {
                        writer.write(line);
                        writer.newLine();
                        continue;
                    }

                    String username = parts[0];

                    if (username.equalsIgnoreCase(CreateUserUsername.getText())) {
                        writer.write(username + ":"
                                + auth.hash(CreateUserPassword.getText().toCharArray()) + ":"
                                + CreateUserAPIkey.getText());
                        writer.newLine();
                        written = true;
                    } else {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                //if username is not already in File a new username will be created
                if (!written) {
                    writer.write(CreateUserUsername.getText() + ":"
                            + auth.hash(CreateUserPassword.getText().toCharArray()) + ":"
                            + CreateUserAPIkey.getText());
                    writer.newLine();
                }
            } catch (IOException ioException) {
            }

            CreateNewUser.getScene().getWindow().hide();
        }
    }
}