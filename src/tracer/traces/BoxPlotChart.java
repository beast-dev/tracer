/*
 * BoxPlotChart.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package tracer.traces;

import dr.app.gui.chart.Axis;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Create box plots and intervals in the same chart.
 *
 * @author Andrew Rambaut
 * @author Walter Xie
 */
//public class BoxPlotChart extends JIntervalsChart {
//
//    private boolean plotOutliers = true;
//
//    protected class BoxPlot extends Interval {
//        double q1, q3, min, max;
//        List<Double> outliers;
//
//        // value is median, upper is Q3 + 1.5 IQR, lower is Q1 - 1.5 IQR
//        BoxPlot(String name, double median, double q3, double q1, double max, double min) {
//            super(name, median);
//            this.q1 = q1;
//            this.q3 = q3;
//            this.min = min;
//            this.max = max;
//
//            double iqr = q3 - q1;
//            super.upper = q3 + 1.5 * iqr;
//            super.lower = q1 - 1.5 * iqr;
//        }
//
//        BoxPlot(String name, double median, double q3, double q1, double max, double min, List<Double> outliers) {
//            this(name, median, q3, q1, max, min);
//            this.outliers = new ArrayList<Double>(outliers);
//        }
//    }
//
////    private final ArrayList<BoxPlot> boxPlots = new ArrayList<BoxPlot>();
//
//    public BoxPlotChart(Axis yAxis) {
//        super(yAxis);
//    }
//
//    public BoxPlotChart(Axis xAxis, Axis yAxis) {
//        super(xAxis, yAxis);
//    }
//
//    public void addBoxPlots(String name, double median, double q1, double q3, double min, double max) {
//
//        intervals.add(new BoxPlot(name, median, q1, q3, min, max));
//
//        xAxis.addRange(1, intervals.size());
//        yAxis.addRange(min, max);
//
//        recalibrate();
//        repaint();
//    }
//
//
//    protected void paintContents(Graphics2D g2) {
//        if (intervals.size() > 0) {
//            for (int i = 0; i < intervals.size(); i++) {
//
//                Interval interval = intervals.get(i);
//
//                if (interval instanceof BoxPlot)
//                    drawBoxPlot(g2, i, (BoxPlot) interval);
//                else
//                    super.drawInterval(g2, i, interval);
//            }
//        } else {
//            super.paintContents(g2);
//        }
//    }
//
//    protected void drawBoxPlot(Graphics2D g2, int i, BoxPlot boxPlot) {
//
//        float x = (float) transformX(i + 1);
//        float xWhiskerLeft = (float) transformX(((double) i + 1) - 0.05);
//        float xWhiskerRight = (float) transformX(((double) i + 1) + 0.05);
//        float xLeft = (float) transformX(((double) i + 1) - 0.1);
//        float xRight = (float) transformX(((double) i + 1) + 0.1);
//        //float y = (float)transformY(interval.value);
//        float yMin = (float) transformY(boxPlot.min);
//        float yMax = (float) transformY(boxPlot.max);
//        float yQ1 = (float) transformY(boxPlot.q1);
//        float yQ3 = (float) transformY(boxPlot.q3);
//        float yWhiskerLower = (float) transformY(boxPlot.lower);
//        float yWhiskerUpper = (float) transformY(boxPlot.upper);
//        // value is median
//        float yMedian = (float) transformY(boxPlot.value);
//
//        GeneralPath path = new GeneralPath();
//        path.moveTo(xWhiskerLeft, yWhiskerUpper);
//        path.lineTo(xWhiskerRight, yWhiskerUpper);
//        path.moveTo(x, yWhiskerUpper);
//        path.lineTo(x, yQ3);
//        path.moveTo(xLeft, yQ3);
//        path.lineTo(xRight, yQ3);
//        path.lineTo(xRight, yQ1);
//        path.lineTo(xLeft, yQ1);
//        path.lineTo(xLeft, yQ3);
//
//        path.moveTo(x, yQ1);
//        path.lineTo(x, yWhiskerLower);
//        path.moveTo(xWhiskerLeft, yWhiskerLower);
//        path.lineTo(xWhiskerRight, yWhiskerLower);
//
//        g2.setStroke(new BasicStroke(1.0f));
//        g2.setPaint(Color.black);
//        g2.draw(path);
//
//        // draw median
//        path = new GeneralPath();
//        path.moveTo(xLeft, yMedian);
//        path.lineTo(xRight, yMedian);
//
//        g2.setStroke(new BasicStroke(2.0f));
//        g2.draw(path);
//    }
//
//}