package Plotter

import Plotter.Core.Plotter
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color

class `2PosPlotter` : Plotter {
    private val xs = mutableListOf<Double>()
    private val ys = mutableListOf<Double>()

    private val targetxs = mutableListOf<Double>()
    private val targetys = mutableListOf<Double>()

    private var xName: String = ""
    private var yName: String = ""

    fun addDataInterceptor(x: Double, y : Double) {
        if (!x.isNaN() && !y.isNaN()) {
            xs.add(x)
            ys.add(y)
        }
    }

    fun addDataTarget(x: Double, y : Double) {
        if (!x.isNaN() && !y.isNaN()) {
            targetxs.add(x)
            targetys.add(y)
        }
    }

    override fun getPlot(): Plot {
        return plot {
            line {
                x(xs) {
                    axis.name = xName
                }

                y(ys) {
                    axis.name = yName
                }

                color = Color.RED
            }

            line {
                x(targetxs) {
                    axis.name = xName
                }

                y(targetys) {
                    axis.name = yName
                }

                color = Color.BLUE
            }
        }
    }

    override fun setXAxisName(s: String) {
        this.xName = s
    }

    override fun setYAxisName(s: String) {
        this.yName = s
    }
}