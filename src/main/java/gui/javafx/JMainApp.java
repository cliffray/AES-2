package gui.javafx;

import crypto.utils.JCryptoUtils;
import enums.JFileStatusEnum;
import gui.javafx.components.JInputFileTableView;
import gui.javafx.components.JNavBar;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jdk.tools.jaotc.Main;
import model.JCipherTransformation;
import model.JInputFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JMainApp extends Application{

    private Button encryptButton, decryptButton;

    private String[] algos = new String[]{"AES", "DES"/*, "3DES"*/};
    private ObservableList<String> algoOptions = FXCollections.observableArrayList(algos);

    private String[] operations = new String[]{"CTR", "ECB" ,"CBC" ,"CFB" ,"CFB8" ,"OFB", "OFB8"};
    private ObservableList<String> operationOptions = FXCollections.observableArrayList(operations);

    private String[] padding = new String[]{"PKCS5Padding", "NoPadding", "ISO10126Padding"};
    private ObservableList<String> paddingOptions = FXCollections.observableArrayList(padding);

    private ObservableList<String> keySizeForAES = FXCollections.observableArrayList(new String[]{"128", "192", "256"});
    private ObservableList<String> keySizeForDES = FXCollections.observableArrayList(new String[]{"56"});

    private JInputFile selectFile;
    private ObservableList<JInputFile> filesData = FXCollections.observableArrayList(

    );

    private final int[] ivSizeForBytes = new int[]{16, 8};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try(Stream<Path> stream = Files.walk(Paths.get(
                (new File(getClass().getResource("").toURI()))
                        .getParentFile()
                        .getParentFile()
                        .getParentFile()
                        .getParentFile()
                        .getParentFile()
                        .toString() + "/resources/main/img/"
        ))){
            List<String> collect = stream.filter(Files::isRegularFile).map(s -> s.toString()).collect(Collectors.toList());
            collect.forEach(s -> filesData.add(new JInputFile(s)));
        }

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(new MenuItem("New"));

        menuBar.getMenus().addAll(fileMenu);

        JNavBar navBar = new JNavBar();

        Label algoTitleLabel = new Label("加密演算法參數 (必填)");
        algoTitleLabel.setStyle("-fx-font-size: 20px");

        ComboBox<String> algorithmComboBox = new ComboBox<>(algoOptions);
        algorithmComboBox.setMinWidth(200.0);
        algorithmComboBox.getSelectionModel().select(0);

        ComboBox<String> operationCombobox = new ComboBox<>(operationOptions);
        operationCombobox.setMinWidth(200.0);
        operationCombobox.getSelectionModel().select(0);

        ComboBox<String> paddingCombobox = new ComboBox<>(paddingOptions);
        paddingCombobox.getItems().remove(padding.length - 1);
        paddingCombobox.setMinWidth(200.0);
        paddingCombobox.getSelectionModel().select(0);

        ComboBox<String> keySizeCombobox = new ComboBox<>(keySizeForAES);
        keySizeCombobox.setMinWidth(200.0);
        keySizeCombobox.getSelectionModel().select(0);

        /*TextField keySizeTextField = new TextField();
        keySizeTextField.setMinWidth(200.0);
        keySizeTextField.setPromptText("ex: 128, 192, 256 for AES");*/

        Label keyAndIvTitleLabel = new Label("使用既有key和iv");
        keyAndIvTitleLabel.setStyle("-fx-font-size: 20px");

        TextField encodedKeyStringTextField = new TextField();
        encodedKeyStringTextField.setMaxWidth(200.0);
        encodedKeyStringTextField.setPromptText("留空或輸入既有key");

        TextField ivStringTextField = new TextField();
        ivStringTextField.setMaxWidth(200.0);
        ivStringTextField.setPromptText("輸入ivSize的數字以「,」分開");

        Label hintForSelectFileLabel = new Label("選取檔案後進行處理");
        hintForSelectFileLabel.setStyle("-fx-font-size: 20px");

        encryptButton = new Button("加密");
        encryptButton.setMinWidth(75.0);

        decryptButton = new Button("解密");
        decryptButton.setMinWidth(75.0);

        navBar.getChildren().addAll(
                algoTitleLabel,
                new Label("演算法 Algorithm"),
                algorithmComboBox,
                new Label("工作模式 Mode of operation"),
                operationCombobox,
                new Label("填充方式 Padding"),
                paddingCombobox,
                new Label("鑰匙長度 Key Size"),
                keySizeCombobox,
                new Label(), // for spacing

                keyAndIvTitleLabel,
                new Label("使用鑰匙"),
                encodedKeyStringTextField,
                new Label("初始向量"),
                ivStringTextField,
                new Label(), // for spacing

                hintForSelectFileLabel,
                new HBox(encryptButton, new Label("  "), decryptButton)
        );

        JInputFileTableView filesTableView = new JInputFileTableView();
        filesTableView.setItems(filesData);
        filesTableView.getSelectionModel().select(0);



        SplitPane contentSplitPane = new SplitPane();
        contentSplitPane.setMinHeight(500.0);
        contentSplitPane.getItems().addAll(navBar, filesTableView);

        // ---------------- add file
//
//        VBox addFileVBox = new VBox(5.0);
//        addFileVBox.setStyle("-fx-padding: 20px");
//
//        TextField completeFileTextField = new TextField();
//        completeFileTextField.setMinWidth(300.0);
//        completeFileTextField.setPromptText("請輸入完整路徑與檔名");
//
//        Button addButton = new Button("新增檔案");
//        addButton.setMinWidth(75.0);
//
//        addFileVBox.getChildren().addAll(
//                new HBox(new Label("路徑與檔名: "), completeFileTextField),
//                addButton
//        );

        // ---------------- scene
        VBox vbox = new VBox();
        vbox.getChildren().addAll(menuBar, contentSplitPane /*,addFileVBox*/);

        Scene scene = new Scene(vbox);

        // ---------------- event

        algorithmComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                switch (t1){
                    case "AES":
                        keySizeCombobox.setItems(keySizeForAES);
                        break;
                    case "DES":
                        keySizeCombobox.setItems(keySizeForDES);
                        break;
                }
                keySizeCombobox.getSelectionModel().select(0);
            }
        });

        operationCombobox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 == "CTR"){
                    paddingCombobox.getItems().remove(padding.length - 1);
                }else if(paddingCombobox.getItems().size() < padding.length){
                    paddingCombobox.getItems().add(padding[padding.length - 1]);
                }
            }
        });

        encryptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int index = filesTableView.getSelectionModel().getSelectedIndex();
                selectFile = filesTableView.getItems().get(index);
                if (Objects.isNull(selectFile)) return;
                selectFile.setEncryptStatus(JFileStatusEnum.PROCESSING);
                filesTableView.refresh();

                JCipherTransformation cipherTransformation = new JCipherTransformation(
                        algorithmComboBox.getValue(),
                        operationCombobox.getValue(),
                        paddingCombobox.getValue(),
                        ivSizeForBytes[algorithmComboBox.getSelectionModel().getSelectedIndex()]
                );
                String encodedKey = encodedKeyStringTextField.getText();
                int keySize = Integer.valueOf(keySizeCombobox.getSelectionModel().getSelectedItem());

                // TODO: AES，設定keySize = 256, ivSize = 128/8 = 16
                try {
                    JCryptoUtils.encrypt(cipherTransformation, keySize, selectFile, encodedKey, ivStringTextField.getText());
                    selectFile.setEncryptStatus(JFileStatusEnum.FINISH);
                } catch (Exception e) {
                    selectFile.setEncryptStatus(JFileStatusEnum.FAILED);
                    System.out.printf("[ERROR] %s\n", e.getLocalizedMessage());
                    e.printStackTrace();
                } finally {
                    filesTableView.refresh();
                }
            }
        });

        decryptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int index = filesTableView.getSelectionModel().getSelectedIndex();
                selectFile = filesTableView.getItems().get(index);
                if (Objects.isNull(selectFile)) return;

                selectFile.setDecryptStatus(JFileStatusEnum.PROCESSING);
                filesTableView.refresh();

                JCipherTransformation cipherTransformation = new JCipherTransformation(
                        algorithmComboBox.getValue(),
                        operationCombobox.getValue(),
                        paddingCombobox.getValue(),
                        ivSizeForBytes[algorithmComboBox.getSelectionModel().getSelectedIndex()]
                );
                String encodedKey = encodedKeyStringTextField.getText();
                int keySize = Integer.valueOf(keySizeCombobox.getSelectionModel().getSelectedItem());

                // TODO: 目前是先以AES, ivSize = 128/8 = 16
                try {
                    JCryptoUtils.decrypt(cipherTransformation, keySize, new JInputFile(String.format("%s/output/%s.enc", new File(selectFile.getCompleteFilename()).getParentFile().getParent(), selectFile.getFilename())), encodedKey, ivStringTextField.getText());
                    selectFile.setDecryptStatus(JFileStatusEnum.FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                    selectFile.setDecryptStatus(JFileStatusEnum.FAILED);
                } finally {
                    filesTableView.refresh();
                }
            }
        });

//        addButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                filesData.add(new JInputFile(completeFileTextField.getText()));
//                filesTableView.refresh();
//            }
//        });

        // ---------------- primary stage
//        primaryStage.icons.add(Image("img/MoAstray_icon.png"))
        primaryStage.setTitle("資訊安全導論 AES");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public void runApp(){
        launch();
    }
}
