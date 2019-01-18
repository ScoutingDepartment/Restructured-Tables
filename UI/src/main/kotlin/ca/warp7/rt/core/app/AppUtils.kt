package ca.warp7.rt.core.app

import ca.warp7.rt.core.env.EnvUtils
import ca.warp7.rt.core.env.UserConfig
import ca.warp7.rt.core.env.UserEnv
import ca.warp7.rt.ext.ast.ASTFeature
import ca.warp7.rt.ext.predictor.PredictorFeature
import ca.warp7.rt.ext.python.PythonFeature
import ca.warp7.rt.ext.scanner.ScannerFeature
import ca.warp7.rt.ext.views.ViewsFeature
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.WindowEvent

@Suppress("unused")
fun unused() {
    PythonFeature()
}

internal val appFeatures = listOf(
        ViewsFeature(),
        ASTFeature(),
        PredictorFeature(),
        ScannerFeature()
)

internal var utilsController: AppController? = null

private val teamColor: Color = Color.valueOf("1e2e4a")

internal fun tabUIFromLink(wrapper: FeatureWrapper): HBox {
    val outer = HBox()
    val inner = HBox()
    inner.prefWidth = 16.0
    inner.alignment = Pos.CENTER
    wrapper.icon.iconColor = teamColor
    wrapper.icon.iconSize -= 2
    inner.children.add(wrapper.icon)
    outer.alignment = Pos.CENTER_LEFT
    outer.spacing = 12.0
    outer.children.add(inner)
    val label = Label(wrapper.link.title)
    label.style = "-fx-font-size:16; -fx-font-weight:bold"
    outer.children.add(label)
    return outer
}

internal fun getUserExplicitName(): String {
    val current = UserEnv["app.userName", EnvUtils.user]
    val name = userInputString("Setup", "Enter name (First Last):",
            current, String::isNotEmpty) ?: current
    UserEnv[UserConfig.appUserName] = name
    return name
}

fun userInputString(title: String, prompt: String, defVal: String,
                    validator: ((value: String) -> Boolean)? = null): String? {

    val dialog = Dialog<String>()

    dialog.title = title
    dialog.initOwner(utilsController?.appStage)

    val vBox = VBox()
    vBox.spacing = 10.0
    vBox.alignment = Pos.TOP_LEFT
    vBox.style = "-fx-padding: 10"

    val label = Label(prompt)
    label.isWrapText = true

    val field = TextField()
    field.text = defVal.trim { it <= ' ' }

    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

    if (validator != null) {
        val okButton = dialog.dialogPane.lookupButton(ButtonType.OK)
        okButton.isDisable = true
        field.textProperty().addListener { _, _, newValue ->
            okButton.isDisable = !validator.invoke(newValue.trim { it <= ' ' })
        }
    }

    vBox.children.addAll(label, field)
    vBox.minWidth = 400.0

    dialog.dialogPane.content = vBox
    dialog.dialogPane.stylesheets.add("/ca/warp7/rt/res/style.css")

    Platform.runLater { field.requestFocus() }

    dialog.setResultConverter { buttonType ->
        if (buttonType == ButtonType.OK) field.text else null
    }

    val result = dialog.showAndWait()

    return result.orElse(null)
}

fun setAppStatus(statusMessage: String) {
    if (utilsController != null) utilsController!!.statusMessageLabel.text = statusMessage
}

fun getUserSettings(): SettingsData? {
    val dialog = Dialog<SettingsData>()

    dialog.title = "Settings"
    dialog.initOwner(utilsController?.appStage)

    val vBox = VBox()
    vBox.spacing = 10.0
    vBox.alignment = Pos.TOP_LEFT
    vBox.style = "-fx-padding: 10"

    val userLabel = Label("User Name (First Last)")

    val userField = TextField()
    userField.text = ""

    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

    vBox.children.addAll(userLabel, userField)

    val tbaLabel = Label("The Blue Alliance API Key")
    val tbaField = TextField()

    vBox.children.addAll(tbaLabel, tbaField)

    vBox.minWidth = 500.0

    dialog.dialogPane.content = vBox

    val window = dialog.dialogPane.scene.window
    window.addEventHandler(WindowEvent.WINDOW_SHOWN) {
        val screenBounds = Screen.getPrimary().visualBounds
        window.x = (screenBounds.width - window.width) / 2
        window.y = (screenBounds.height - window.height) / 2
    }

    Platform.runLater {
        userField.requestFocus()
    }

    dialog.setResultConverter {
        if (it == ButtonType.OK) {
            SettingsData(userField.text, tbaField.text)
        } else null
    }

    return dialog.showAndWait().orElse(null)
}