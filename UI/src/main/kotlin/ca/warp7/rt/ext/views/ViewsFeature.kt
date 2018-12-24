package ca.warp7.rt.ext.views

import ca.warp7.rt.core.feature.Feature
import ca.warp7.rt.core.feature.FeatureLink
import ca.warp7.rt.core.feature.FeatureUtils
import javafx.scene.Parent

class ViewsFeature : Feature {

    private var preLoaded: Parent? = null

    override val link = FeatureLink("Table Views", "fas-eye", 16)

    override fun onOpen(): Pair<Parent?, Parent?> {
        if (preLoaded == null) {
            preLoaded = FeatureUtils.loadParent("/ca/warp7/rt/ext/views/Views.fxml")
        }
        return null to preLoaded
    }
}