package Plotter

import Plotter.Core.Plotter
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import time

class TimePlotter : Plotter {
    private var recorded = mutableListOf<Double>()
    private val recordedTime = mutableListOf<Double>()
    private var yName : String = ""

    fun addDataAtTime(x: Double) {
        if (!x.isNaN()) {
            recorded.add(x)
            recordedTime.add(time)
        }
    }

    override fun getPlot(): Plot {
        return plot {
            line {
                x(recordedTime) {
                    axis.name = "Time elapsed"
                }
                y(recorded) {
                    axis.name = yName
                }
            }
        }
    }

    override fun setXAxisName(s: String) {
    }

    override fun setYAxisName(s: String) {
        this.yName = s
    }

    fun getPreviousEntry() : Double {
        return try { recorded.last() } catch (_ : Exception) { 0.0 }
    }

    fun transform(f : (Double) -> Double) {
        this.recorded = this.recorded.map {x -> f(x)}.toMutableList()
    }
}