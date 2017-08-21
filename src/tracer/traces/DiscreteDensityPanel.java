/*
 * DensityPanel.java
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

import dr.app.gui.chart.*;
import dr.inference.trace.*;
import dr.stats.Variate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;

/**
 * A panel that displays density plots of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public class DiscreteDensityPanel extends TraceChartPanel {

    private final DiscreteJChart densityChart;
    private final JChartPanel densityChartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private class Settings extends TraceChartPanel.Settings {
        int barCount = 0;
    }

    private Settings currentSettings = new Settings();

    /**
     * Creates new FrequencyPanel
     */
    public DiscreteDensityPanel(final JFrame frame) {
        super(frame);
        densityChart = new DiscreteJChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        densityChartPanel = new JChartPanel(densityChart, "","","");
        JToolBar toolBar = createToolBar(currentSettings);
        setupMainPanel(toolBar);
    }

    protected JChart getChart() {
        return densityChart;
    }

    protected JChartPanel getChartPanel() {
        return densityChartPanel;
    }

    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(frame, false, false, false, false,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected TraceChartPanel.Settings getSettings() {
        return currentSettings;
    }

    private JToolBar createToolBar(Settings settings) {
        JToolBar toolBar = super.createToolBar();

        toolBar.add(createSetupButton());

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        JLabel label = (JLabel)createLegendComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.legendAlignment);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        label = (JLabel)createColourByComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.colourBy.ordinal());

        return toolBar;
    }

    public void setTraces(TraceList[] traceLists, List<String> traceNames) {
        super.setTraces(traceLists, traceNames);

//        legendCombo.setSelectedIndex(currentSettings.legendAlignment);
//        colourByCombo.setSelectedIndex(currentSettings.colourBy.ordinal());


        if (traceLists != null) {
//        barCount = 0;
            TraceType traceType = null;
            for (TraceList tl : traceLists) {
                for (String traceName : traceNames) {
                    int traceIndex = tl.getTraceIndex(traceName);
                    Trace trace = tl.getTrace(traceIndex);
                    if (trace != null) {
                        if (traceType == null) {
                            traceType = trace.getTraceType();
                        }
                        if (trace.getTraceType() != traceType) {
                            getChart().removeAllPlots();

                            getChartPanel().setXAxisTitle("");
                            getChartPanel().setYAxisTitle("");
                            messageLabel.setText("Unable to display a mixture statistics types.");
                            return;
                        }
                        messageLabel.setText("");
                    }
                }
            }

        }

        setupTraces();
    }

    @Override
    protected void setupTraces() {
        // return if no traces selected
        if (!removeAllPlots(false)) return;

        int barId = 0;
        int i = 0;
        TraceType traceType = null;
        for (TraceList tl : traceLists) {
            int n = tl.getStateCount();

            for (String traceName : traceNames) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);
                TraceCorrelation td = tl.getCorrelationStatistics(traceIndex);
                Plot plot = null;

                if (trace != null) {
                    String name = tl.getTraceName(traceIndex);
                    if (traceLists.length > 1) {
                        name = tl.getName() + " - " + name;
                    }

                    List values = tl.getValues(traceIndex);

                    // set traceType here to avoid Exception from setYLabel
                    traceType = trace.getTraceType();
                    if (traceType.isContinuous()) {
                        throw new IllegalArgumentException("DiscreteDensityPanel is not for continous variables");
                    } else if (traceType.isInteger()) {
                        plot = new CategoryDensityPlot(values, -1, td, currentSettings.barCount, barId);
                        barId++;

                    } else if (traceType.isCategorical()) {
                        plot = new CategoryDensityPlot(values, td, currentSettings.barCount, barId);
                        barId++;

                    } else {
                        throw new RuntimeException("Trace type is not recognized: " + traceType);
                    }

                    if (plot != null) {
                        plot.setName(name);
                        if (tl instanceof CombinedTraces) {
                            plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[i]);
                        } else {
                            plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[i]);
                        }

                        getChart().addPlot(plot);
                    }
                    // change x axis to DiscreteAxis or LinearAxis according TraceType
                    setXAxis(td);

                    // colourBy
                    if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE || currentSettings.colourBy == ColourByOptions.COLOUR_BY_ALL) {
                        i++;
                    }
                    if (i == currentSettings.palette.length) i = 0;
                }
            }
            if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE) {
                i++;
            } else if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE) {
                i = 0;
            }
            if (i >= currentSettings.palette.length) i = 0;
        }

        setXLabelMultipleTraces();
        setYLabel(traceType, new String[]{"Density", "Probability"});

        validate();
        repaint();
    }

    protected void setXAxis(TraceDistribution td) {
        if (td != null) {
            TraceType traceType = td.getTraceType();

            if (!(getChart() instanceof DiscreteJChart)) {
                throw new RuntimeException("traceChart has to be instanceof DiscreteJChart, " +
                        "using setXAxis(TraceType traceType, Map<Integer, String> categoryDataMap) !");
            }

            if (!traceType.isCategorical()) {
                ((DiscreteJChart) getChart()).setXAxis(traceType.isIntegerOrBinary(),
                        new HashMap<Integer, String>());

            } else if (traceType.isCategorical()) {
                ((DiscreteJChart) getChart()).setXAxis(false, td.getIndexMap());

            } else {
                throw new RuntimeException("Trace type is not recognized: " + traceType);
            }
        }
    }

    /**
     * set x labs using <code>setXAxisTitle</code> when x-axis allows multiple traces
     */
    protected void setXLabelMultipleTraces() {
        if (traceLists.length == 1) {
            getChartPanel().setXAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            getChartPanel().setXAxisTitle(traceNames.get(0));
        } else {
            getChartPanel().setXAxisTitle("Multiple Traces");
        }
    }
    /**
     * set y labs using <code>setYAxisTitle</code> when y-axis allows multiple traces
     */
    protected void setYLabelMultipleTraces() {
        if (traceLists.length == 1) {
            getChartPanel().setYAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            getChartPanel().setYAxisTitle(traceNames.get(0));
        } else {
            getChartPanel().setYAxisTitle("Multiple Traces");
        }
    }
    public String toString() {
        JChart chart = getChart();

        if (chart.getPlotCount() == 0) {
            return "no plot available";
        }

        StringBuffer buffer = new StringBuffer();

        Plot plot = chart.getPlot(0);
        Variate xData = plot.getXData();

        buffer.append(getChartPanel().getXAxisTitle());
        for (int i = 0; i < chart.getPlotCount(); i++) {
            plot = chart.getPlot(i);
            buffer.append("\t");
            buffer.append(plot.getName());
        }
        buffer.append("\n");

        for (int i = 0; i < xData.getCount(); i++) {
            buffer.append(String.valueOf(xData.get(i)));
            for (int j = 0; j < chart.getPlotCount(); j++) {
                plot = chart.getPlot(j);
                Variate yData = plot.getYData();
                buffer.append("\t");
                buffer.append(String.valueOf(yData.get(i)));
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }

}
