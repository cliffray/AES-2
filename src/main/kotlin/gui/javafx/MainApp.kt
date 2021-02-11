package gui.javafx

import crypto.utils.CryptoUtils
import enums.FileStatusEnum
import gui.javafx.components.NavBar
import gui.javafx.components.InputFileTableView
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import model.CipherTransformation
import model.InputFile
import java.util.*

/**
 * 專案GUI:javafx入口
 * @property encryptButton Button 點擊後，執行加密
    * @property decryptButton Button 點擊後，執行解密
    * @property algos Array<String> 對稱加密演算法的名稱
    * @property algoOptions (javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>..javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>?)
    * @property operations Array<String> 加密演算法工作模式的名稱
    * @property operationOptions (javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>..javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>?)
    * @property padding Array<String> 加密演算法填充方式的名稱
    * @property paddingOptions (javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>..javafx.collections.ObservableList<(kotlin.String..kotlin.String?)>?)
    * @property selectFile InputFile TableView選取的檔案
    * @property filesData (javafx.collections.ObservableList<(model.InputFile..model.InputFile?)>..javafx.collections.ObservableList<(model.InputFile..model.InputFile?)>?)
    */
    class MainApp :  Application() {

    // env
//    private val path = System.getProperty("user.dir")

    // gui:components
    private lateinit var encryptButton: Button
    private lateinit var decryptButton: Button

    // gui:stage
//    private var offsetX = .0
//    private var offsetY = .0

    // gui:algo/operation/padding options
    private val algos = arrayOf("AES", "DES", "3DES")
    private val algoOptions = FXCollections.observableArrayList(*algos) // *: collection to varargs

    private val operations = arrayOf("CTR", "ECB" ,"CBC" ,"PCBC" ,"CTS" ,"CFB", "CFB8" ,"OFB", "OFB8")
    private val operationOptions = FXCollections.observableArrayList(*operations)

    private val padding = arrayOf("PKCS5Padding", "NoPadding", "ISO10126Padding")
    private val paddingOptions = FXCollections.observableArrayList(*padding)

    // file
    private lateinit var selectFile: InputFile
    private val filesData = FXCollections.observableArrayList<InputFile>(
        InputFile("src/main/resources/img/MoAstray_icon.png"),
        InputFile("src/main/resources/output/MoAstray_icon.png.enc"),
        InputFile("src/main/resources/img/Mo08.webp")
    )

    /**
     * 初始化各種GUI
     * @param primaryStage Stage
     * @return Unit
     */
    override fun start(primaryStage: Stage) {
        // ---------------- custom tool bar (deprecated)
        /* Do note that because you are implementing your own titlebar, you will also lose the OS's default window drag behavior, so you will need to implement your own window-drag/window-move code. This answer might be able to help you with that:
        https://stackoverflow.com/questions/11780115/moving-an-undecorated-stage-in-javafx-2/11781291#11781291
         */
        /*
        // remove window decoration
        primaryStage.initStyle(StageStyle.UNDECORATED)

        val borderPane = BorderPane()
        borderPane.style = "-fx-background-color: #aa11ee"
        borderPane.setOnMousePressed { event ->
            offsetX = primaryStage.x - event.sceneX
            offsetY = primaryStage.y - event.sceneY
            println("$offsetX $offsetY")
        }

        borderPane.setOnMouseReleased { event ->
            primaryStage.x = event.sceneX
            primaryStage.y = event.sceneY
            println("setOnMouseReleased")
        }

        borderPane.setOnMouseDragOver { event ->
            primaryStage.x = event.sceneX + offsetX
            primaryStage.y = event.sceneY + offsetY
            println("setOnMouseDragOver")
        }

        val toolBar = ToolBar()

        val toolbarHeight = 25.0
        toolBar.prefHeight = toolbarHeight
        toolBar.minHeight = toolbarHeight
        toolBar.maxHeight = toolbarHeight
        toolBar.items.add(WindowButtons())

        borderPane.top = toolBar
         */

        // ---------------- content
        // menu bar
        val menuBar = MenuBar()

        val fileMenu = Menu("File")
        fileMenu.items.add(MenuItem("New"))

        menuBar.menus.addAll(fileMenu)

        val navBar = NavBar()

        val algoTitleLabel = Label("加密演算法參數 (必填)")
        algoTitleLabel.style = "-fx-font-size: 20px"

        val algorithmComboBox = ComboBox<String>(algoOptions)
        algorithmComboBox.minWidth = 200.0
        algorithmComboBox.selectionModel.select(0)

        val operationCombobox = ComboBox<String>(operationOptions)
        operationCombobox.minWidth = 200.0
        operationCombobox.selectionModel.select(0)

        val paddingCombobox = ComboBox<String>(paddingOptions)
        paddingCombobox.minWidth = 200.0
        paddingCombobox.selectionModel.select(0)

        val keySizeTextField = TextField()
        keySizeTextField.minWidth = 200.0
        keySizeTextField.promptText = "ex: 128, 192, 256 for AES"

        val keyAndIvTitleLabel = Label("使用既有key和iv")
        keyAndIvTitleLabel.style = "-fx-font-size: 20px"

        val encodedKeyStringTextField = TextField()
        encodedKeyStringTextField.minWidth = 200.0
        encodedKeyStringTextField.promptText = "留空或輸入既有key"

        val ivStringTextField = TextField()
        ivStringTextField.minWidth = 200.0
        ivStringTextField.promptText = "輸入ivSize的數字以「,」分開"

        val hintForSelectFileLabel = Label("選取檔案後進行處理")
        hintForSelectFileLabel.style = "-fx-font-size: 20px"

        encryptButton = Button("加密")
        encryptButton.minWidth = 75.0

        decryptButton = Button("解密")
        decryptButton.minWidth = 75.0

        navBar.children.addAll(
            algoTitleLabel,
            Label("演算法 Algorithm"),
            algorithmComboBox,
            Label("工作模式 Mode of operation"),
            operationCombobox,
            Label("填充方式 Padding"),
            paddingCombobox,
            Label("鑰匙長度 Key Size"),
            keySizeTextField,
            Label(), // for spacing

            keyAndIvTitleLabel,
            Label("使用鑰匙"),
            encodedKeyStringTextField,
            Label("初始向量"),
            ivStringTextField,
            Label(), // for spacing

            hintForSelectFileLabel,
            HBox(encryptButton, Label("  "), decryptButton)
        )

        val filesTableView = InputFileTableView()
        filesTableView.items = filesData
        filesTableView.selectionModel.select(0)

        val infoPane = VBox()
        val infoImageView = ImageView("img/info_bg.png")
        infoImageView.fitWidthProperty().bind(infoPane.widthProperty())
        infoImageView.fitHeightProperty().bind(infoPane.heightProperty())
//        infoText.wrappingWidthProperty().bind(infoPane.widthProperty()) // 自動換行 wrap
        infoPane.minWidth = 200.0
        infoPane.minHeight = 500.0
        infoPane.children.add(infoImageView)

        val contentSplitPane = SplitPane()
        contentSplitPane.minHeight = 500.0
        contentSplitPane.items.addAll(navBar, filesTableView, infoPane)

        // ---------------- add file

        val addFileVBox = VBox(5.0)
        addFileVBox.style = "-fx-padding: 20px"

        val completeFileTextField = TextField()
        completeFileTextField.minWidth = 300.0
        completeFileTextField.promptText = "請輸入完整路徑與檔名"

        val addButton = Button("新增檔案")
        addButton.minWidth = 75.0

        addFileVBox.children.addAll(
            HBox(Label("路徑與檔名: "), completeFileTextField),
            addButton
        )

        // ---------------- scene
        val vbox = VBox()
        vbox.children.addAll(menuBar, contentSplitPane, addFileVBox)

        val scene = Scene(vbox)

        // ---------------- event
        encryptButton.setOnAction {
            val index = filesTableView.selectionModel.selectedIndex
            selectFile = filesTableView.items[index]
            if (Objects.isNull(selectFile)) return@setOnAction

            selectFile.setEncryptStatus(FileStatusEnum.PROCESSING)
            filesTableView.refresh()

            val cipherTransformation = CipherTransformation(algorithmComboBox.value, operationCombobox.value, paddingCombobox.value)
            val encodedKey = encodedKeyStringTextField.text
            val originalCompleteFilename = selectFile.getCompleteFilename()
            val keySize = keySizeTextField.text.toInt()
            
            // TODO: AES，設定keySize = 256, ivSize = 128/8 = 16
            try {
                CryptoUtils.encrypt(cipherTransformation, keySize, 16, originalCompleteFilename, encodedKey, ivStringTextField.text)
                selectFile.setEncryptStatus(FileStatusEnum.FINISH)
            } catch (e: Exception) {
                selectFile.setEncryptStatus(FileStatusEnum.FAILED)
                println("[ERROR] ${e.localizedMessage}")
            } finally {
                filesTableView.refresh()
            }
        }

        decryptButton.setOnAction {
            val index = filesTableView.selectionModel.selectedIndex
            selectFile = filesTableView.items[index]
            if (Objects.isNull(selectFile)) return@setOnAction

            selectFile.setDecryptStatus(FileStatusEnum.PROCESSING)
            filesTableView.refresh()

            val cipherTransformation = CipherTransformation(algorithmComboBox.value, operationCombobox.value, paddingCombobox.value)
            val encodedKey = encodedKeyStringTextField.text
            val keySize = keySizeTextField.text.toInt()
            val originalCompleteFilename = selectFile.getCompleteFilename()

            // TODO: 目前是先以AES, ivSize = 128/8 = 16
            try {
                CryptoUtils.decrypt(cipherTransformation, keySize, 16, originalCompleteFilename, encodedKey, ivStringTextField.text)
                selectFile.setDecryptStatus(FileStatusEnum.FINISH)
            } catch (e: Exception) {
                selectFile.setDecryptStatus(FileStatusEnum.FAILED)
            } finally {
                filesTableView.refresh()
            }
        }

        addButton.setOnAction {
            filesData.add(InputFile(completeFileTextField.text))
            filesTableView.refresh()
        }

        // ---------------- primary stage
//        primaryStage.icons.add(Image("img/MoAstray_icon.png"))
        primaryStage.title = "資訊安全導論 AES"
        primaryStage.scene = scene
        primaryStage.isMaximized = true
        primaryStage.show()
    }

    /**
     * 啟動app
     * @return Unit
     */
    fun runApp() {
        launch()
    }

//    inner class WindowButtons : HBox() {
//        init {
//            val closeBtn = Button("X")
//
//            closeBtn.onAction = EventHandler {
//                Platform.exit()
//            }
//
//            this.children.add(closeBtn)
//        }
//    }
}