package ca.warp7.rt.ext.views

import javafx.application.Platform
import javafx.scene.layout.BorderPane
import krangl.DataFrame
import krangl.readDelim
import org.apache.commons.csv.CSVFormat

class TableController {

    lateinit var tableContainer: BorderPane

    fun initialize() {
        Platform.runLater {
            val inputStream = javaClass.getResourceAsStream("/ca/warp7/rt/res/test.csv")
            val df: DataFrame = DataFrame.readDelim(
                    inStream = inputStream,
                    format = CSVFormat.DEFAULT.withHeader(),
                    isCompressed = false,
                    colTypes = mapOf())
            val sheet = DataFrameView(df)
            tableContainer.center = sheet
        }
        tableContainer.requestLayout()
    }
}

