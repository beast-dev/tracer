/*
 * BubbleChart.java
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
import dr.app.gui.chart.DiscreteAxis;
import dr.app.gui.chart.JChart;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Map;

/**
 * Create a string of bubbles to visualise categorical values.
 * TangHuLu is a traditional Chinese snack of candied fruit
 * normally sugar-coated hawthorns on a stick.
 *
 * @author Andrew Rambaut
 * @author Walter Xie
 */
public class TangHuLuChart extends JChart {

    private class TangHuLu {
        String name;
        final Map<Integer, String> categoryDataMap;

        TangHuLu(String name, Map<Integer, String> categoryDataMap) {
            this.name = name;
            if (categoryDataMap == null)
                throw new IllegalArgumentException("Categorical data map is null !");
            this.categoryDataMap = categoryDataMap;
        }


    }

    private final ArrayList<TangHuLu> tangHuLus = new ArrayList<TangHuLu>();

    public TangHuLuChart() {
        this(new DiscreteAxis(true, true));
    }

    public TangHuLuChart(Axis yAxis) {
        super(new DiscreteAxis(true, true), yAxis);
        // reset to empty
        tangHuLus.clear();
    }

    public void addTangHuLu(String name, Map<Integer, String> categoryDataMap) {

        tangHuLus.add(new TangHuLu(name, categoryDataMap));

        xAxis.addRange(1, tangHuLus.size());

        recalibrate();
        repaint();
    }

    public void removeAllTangHuLus() {
        tangHuLus.clear();
        xAxis.setRange(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        yAxis.setRange(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        removeAllPlots();
        recalibrate();
        repaint();
    }

    protected void calibrate(Graphics2D g2, Dimension size) {
    }

    protected boolean hasContents() {
        if (tangHuLus.size() > 0) {
            return tangHuLus.size() > 0;
        } else {
            return super.hasContents();
        }
    }

    protected void paintMajorTick(Graphics2D g2, double value, String label, boolean horizontalAxis) {
        if (tangHuLus.size() > 0) {
            g2.setPaint(getAxisPaint());
            g2.setStroke(getAxisStroke());

            if (label == null) label = " ";

            if (horizontalAxis) {
                double pos = transformX(value);

                Line2D line = new Line2D.Double(pos, getPlotBounds().getMaxY(), pos, getPlotBounds().getMaxY() + getMajorTickSize());

                g2.draw(line);

                g2.setPaint(getLabelPaint());
                double width = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, (float) (pos - (width / 2)), (float) (getPlotBounds().getMaxY() + (getMajorTickSize() * 1.25) + getXTickLabelOffset()));
            } else {
                double pos = transformY(value);

                Line2D line = new Line2D.Double(getPlotBounds().getMinX(), pos, getPlotBounds().getMinX() - getMajorTickSize(), pos);
                g2.draw(line);

                g2.setPaint(getLabelPaint());
                double width = g2.getFontMetrics().stringWidth(label);

                if (label == null) label = yAxis.format(value);
                g2.drawString(label, (float)(getPlotBounds().getMinX() - width - (getMajorTickSize() * 1.25)), (float)(pos + getYTickLabelOffset()));
            }
        } else {
            super.paintMajorTick(g2, value, horizontalAxis);
        }
    }

    protected void paintAxis(Graphics2D g2, Axis axis, boolean horizontalAxis) {
        if (tangHuLus.size() > 0) {
            TangHuLu tangHuLu = tangHuLus.get(0);
            Map<Integer, String> categoryDataMap = tangHuLu.categoryDataMap;

//            int index = ((int) value) - 1;
//            TangHuLu tangHuLu = tangHuLus.get(index);
//            String label = tangHuLu.name;



            if ((!categoryDataMap.isEmpty()) && axis.getIsDiscrete()) {
                int n1 = axis.getMajorTickCount();
                int n2, i, j;

                n2 = axis.getMinorTickCount(-1);

                for (i = 0; i < n1; i++) {
                    paintMajorTick(g2, axis.getMajorTickValue(i), categoryDataMap.get((int) axis.getMajorTickValue(i)), horizontalAxis);
                    n2 = axis.getMinorTickCount(i);

                    if (i == (n1 - 1) && axis.getLabelLast()) { // Draw last minor tick as a major one

                        paintMajorTick(g2, axis.getMinorTickValue(0, i), categoryDataMap.get((int) axis.getMinorTickValue(0, i)), horizontalAxis);

                        for (j = 1; j < n2; j++) {
                            paintMinorTick(g2, axis.getMinorTickValue(j, i), horizontalAxis);
                        }
                    } else {

                        for (j = 0; j < n2; j++) {
                            paintMinorTick(g2, axis.getMinorTickValue(j, i), horizontalAxis);
                        }
                    }
                }
            } else {
                super.paintAxis(g2, axis, horizontalAxis);
            }
        }
    }

    protected void paintContents(Graphics2D g2) {
        if (tangHuLus.size() > 0) {
            for (int i = 0; i < tangHuLus.size(); i++) {

                TangHuLu tangHuLu = tangHuLus.get(i);

                for (Map.Entry<Integer, String> entry : tangHuLu.categoryDataMap.entrySet()) {
                    Integer key = entry.getKey();
                    String value = entry.getValue();

                    double x =  transformX(i + 1);
                    double xLeft =  transformX(((double) i + 1) - 0.1);
                    double xRight =  transformX(((double) i + 1) + 0.1);
                    double y = transformY(key);
//                    double yUpper =  transformY(tangHuLu.upper);
//                    double yLower =  transformY(tangHuLu.lower);


                    Ellipse2D ellipse = new Ellipse2D.Double(x, y, 1, 1);

//                    GeneralPath path = new GeneralPath();
//                    path.moveTo(xLeft, yUpper);
//                    path.lineTo(xRight, yUpper);
//                    path.moveTo(x, yUpper);
//                    path.lineTo(x, yLower);
//                    path.moveTo(xLeft, yLower);
//                    path.lineTo(xRight, yLower);

//                if (tangHuLu.bold) {
//                    g2.setStroke(new BasicStroke(2.0f));
//                } else {
//                    g2.setStroke(new BasicStroke(1.0f));
//                }
                    g2.setPaint(Color.black);
                    g2.draw(ellipse);

                }

            }
        } else {
            super.paintContents(g2);
        }

    }

}