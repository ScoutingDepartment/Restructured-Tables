package ca.warp7.android.scouting.v5.entry

interface MutableEntry : Entry {
    /**
     * Adds a data point to the entry
     */
    fun add(dataPoint: DataPoint)

    /**
     * Performs an undo action on the data stack
     *
     * @return the data constant(metrics) of the datum being undone, or null
     * if nothing can be undone
     */
    fun undo(): DataPoint?

    var undone: Int

    override var comments: String

    override var timestamp: Int
}