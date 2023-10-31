package com.example.debilwillcry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AppController {
    private static final Client client;

    static {
        try {
            client = new Client("localhost", 42069);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final ObservableList<String> modes = FXCollections.observableArrayList("ECB", "CBC", "CFB", "OFB", "CTR", "RD", "RD+H");
    String filepath = "";
    String keypath = "";

    @FXML
    private Button decryptButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button downloadButton;

    @FXML
    private TextField fileTextField;

    @FXML
    private ListView<String> filesListView;

    @FXML
    private TextField keyTextField;

    @FXML
    private Button loadFileButton;

    @FXML
    private Button loadKeyButton;

    @FXML
    private ChoiceBox<String> modeChoiceBox;

    @FXML
    private Button uploadButton;

    void setFilePath(String filepath) {
        this.filepath = filepath;
    }

    void setKeyPath(String keypath) {
        this.keypath = keypath;
    }

    @FXML
    void initialize() {
        modeChoiceBox.setItems(modes);
        modeChoiceBox.setValue("ECB");

        try {
            filesListView.setItems(FXCollections.observableArrayList(client.getFileList()));
        } catch (IOException e) {
            filesListView.setItems(FXCollections.observableArrayList("Error occurred while retrieving the list of files"));
        }


        loadFileButton.setOnAction(actionEvent -> {
            fileTextField.clear();
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                setFilePath(file.getAbsolutePath());
                fileTextField.appendText(filepath);
            }
        });

        loadKeyButton.setOnAction(actionEvent -> {
            keyTextField.clear();
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                setKeyPath(file.getAbsolutePath());
                keyTextField.appendText(keypath);
            }
        });

        uploadButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try {
                    client.uploadFile(file.getPath(), modeChoiceBox.getValue());
                    filesListView.setItems(FXCollections.observableArrayList(client.getFileList()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        downloadButton.setOnAction(actionEvent -> {
            if (filesListView.getSelectionModel().getSelectedItem() != null) {
                try {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    File directory = directoryChooser.showDialog(null);
                    if (directory != null)
                        client.downloadFile(filesListView.getSelectionModel().getSelectedItem(), directory.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        deleteButton.setOnAction(actionEvent -> {
            if (filesListView.getSelectionModel().getSelectedItem() != null) {
                try {
                    client.deleteFile(filesListView.getSelectionModel().getSelectedItem());
                    filesListView.setItems(FXCollections.observableArrayList(client.getFileList()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        decryptButton.setOnAction(actionEvent -> {
            if (!Objects.equals(filepath, "") && !Objects.equals(keypath, "")) {
                try {
                    byte[] content = Client.symmetricDecrypt(filepath, keypath, modeChoiceBox.getValue());
                    FileHandler.write(filepath, content, "_decrypted");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}