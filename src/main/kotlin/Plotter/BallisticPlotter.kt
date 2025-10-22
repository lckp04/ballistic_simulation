package Plotter

import Plotter.Core.Plotter
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color

class BallisticPlotter : Plotter {
    private val xs = mutableListOf<Double>()
    private val altitude = mutableListOf<Double>()
    private var color : Color = Color.RED

    fun addData(x: Double, alt : Double) {
        if (!x.isNaN() && !alt.isNaN()) {
            xs.add(x)
            altitude.add(alt)
        }
    }

    override fun getPlot(): Plot {
        return plot {
            line {
                x(xs) {
                    axis.name = "Distance from launch"
                }

                y(altitude) {
                    axis.name = "Altitude"
                }

                color = this.color
            }
        }
    }

    override fun setXAxisName(s: String) {
    }

    override fun setYAxisName(s: String) {
    }

    override fun saveTo(fileName: String) {
        super.saveTo("ballistic_simulation/$fileName")
    }

    fun overrideColor(color : Color) {
        this.color = color
    }
}