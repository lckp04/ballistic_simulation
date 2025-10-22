package Plotter

/**
 * For a particular value, we may want to record how it changes throughout space and distance. This contains sub-loggers
 */
class SpaceTimeLogger(val name : String) {
    private val timeLogger : TimePlotter = TimePlotter()
    private val spaceLogger : `2PosPlotter` = `2PosPlotter`()

    fun saveToFolder(path : String) {
        timeLogger.saveTo("$path/$name/time")
        spaceLogger.saveTo("$path/$name/path")
    }

    fun addData(x : Double, y : Double) {
        timeLogger.addDataAtTime(y)
        spaceLogger.addDataTarget(x, y)
    }
}