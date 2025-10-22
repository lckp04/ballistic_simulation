package Plotter.Core

import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save

interface Plotter {
    fun getPlot() : Plot

    fun setXAxisName(s : String) : Unit
    fun setYAxisName(s : String) : Unit

    fun saveTo(fileName : String) {
        getPlot().save("$fileName.png")
    }
}