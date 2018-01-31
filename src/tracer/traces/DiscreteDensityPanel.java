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
import java.util.HashSet;
import java.util.Set;

/**
 * A panel that displays density plots of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public class DiscreteDensityPanel extends TraceChartPanel {

    private final JChart densityChart;
    private final JChartPanel densityChartPanel;

    private ChartSetupDialog chartSetupDialog = null;

    private JButton setupButton;

    private class Settings extends TraceChartPanel.Settings {
        int barCount = 0;
    }

    private Settings currentSettings = new Settings();

    private JToolBar toolBar;

    /**
     * Creates new DiscreteDensityPanel
     */
    public DiscreteDensityPanel(final JFrame frame) {
        super(frame);
        densityChart = new JChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis(Axis.AT_ZERO, Axis.AT_MAJOR_TICK_PLUS));
        densityChartPanel = new JChartPanel(densityChart, "","","");
        toolBar = createToolBar(currentSettings);
    }

    protected JChart getChart() {
        return densityChart;
    }

    protected JChartPanel getChartPanel() {
        return densityChartPanel;
    }

    protected ChartSetupDialog getChartSetupDialog() {
        if (chartSetupDialog == null) {
            chartSetupDialog = new ChartSetupDialog(getFrame(), false, false, true, false,
                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
        }
        return chartSetupDialog;
    }

    @Override
    protected TraceChartPanel.Settings getSettings() {
        return currentSettings;
    }

    @Override
    protected JToolBar getToolBar() {
        return toolBar;
    }

    private JToolBar createToolBar(Settings settings) {
        JToolBar toolBar = super.createToolBar();

        setupButton = createSetupButton();
        toolBar.add(setupButton);

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

    @Override
    protected void setupTraces() {


        getChart().removeAllPlots();

        TraceType traceType = null;
        Set<String> categoryLabels = null;

        for (TraceList tl : getTraceLists()) {

            for (String traceName : getTraceNames()) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);

                TraceCorrelation td = tl.getCorrelationStatistics(traceIndex);

                Plot plot;

                if (traceType == null) {
                    traceType = trace.getTraceType();
                }

                if (traceType != trace.getTraceType()) {
                    setMessage("Traces must be of the same type to visualize here.");
                    return;
                }

                ColumnPlot columnPlot;

                if (traceType.isCategorical()) {
                    Set<String> labels = new HashSet<String>(trace.getCategoryLabelMap().values());
                    if (categoryLabels == null) {
                        categoryLabels = labels;
                    }
                    labels.retainAll(categoryLabels);
                    if (labels.size() == 0) {
                        setMessage("Categorical traces must have common values to visualize here.");
                        return;
                    }
                    categoryLabels.addAll(trace.getCategoryLabelMap().values());

                    trace.setOrderType(Trace.OrderType.FREQUENCY);
                    columnPlot = new ColumnPlot(trace.getFrequencyCounter(), trace.getCategoryOrder(), false);

                    Set<Integer> credibleSet = trace.getTraceStatistics().getCredibleSet();
                    columnPlot.setIntervals(0, credibleSet.size());

                    columnPlot.setColumnWidth(0.95);

                    getChartPanel().getChart().setXAxis(new DiscreteAxis(trace.getCategoryLabelMap(), true, true));

                } else {
                    columnPlot = new ColumnPlot(trace.getFrequencyCounter(),  null, false);

                    columnPlot.setIntervals(trace.getTraceStatistics().getLowerHPD(), trace.getTraceStatistics().getUpperHPD());
                    columnPlot.setColumnWidth(0.75);

                    Axis xAxis = new DiscreteAxis(true, trace.getUniqueValueCount() < 20);
                    getChartPanel().getChart().setXAxis(xAxis);

                    if (trace.getUniqueValueCount() == 1) {
                        xAxis.addRange(0, 1);
                    }
                }

                if (columnPlot != null) {
                    String name = tl.getTraceName(traceIndex);
                    if (getTraceLists().length > 1) {
                        name = tl.getName() + " - " + name;
                    }

                    columnPlot.setName(name);
                    
                    int selectedColour = currentSettings.cm.addTraceColour(tl.getFullName(), name, currentSettings.colourBy);
                    if (tl instanceof CombinedTraces) {
                        columnPlot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[selectedColour]);
                    } else {
                        columnPlot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[selectedColour]);
                    }
                    columnPlot.setPaints(createTranslucentColor((Color)currentSettings.palette[selectedColour], 128), createTranslucentColor((Color)currentSettings.palette[selectedColour], 32));


                    plot = columnPlot;

                    getChart().setOriginStyle(null, null);
                    getChart().addPlot(plot);
                }

                // change x axis to DiscreteAxis or LinearAxis according TraceType
                setXAxis(trace, td);

            }
        }

        setupButton.setEnabled(traceType != TraceType.CATEGORICAL);

        setXLabelMultipleTraces();
        setYLabel(traceType, new String[]{"Density", "Probability"});
        setLegend(currentSettings.legendAlignment);

        validate();
        repaint();
    }

    private Color createTranslucentColor(Color source, int alpha) {
        return new Color(
                ((Color) source).getRed(),
                ((Color) source).getGreen(),
                ((Color) source).getBlue(), alpha);
    }

    protected void setXAxis(Trace trace, TraceDistribution td) {
        if (td != null) {
            TraceType traceType = td.getTraceType();

            if (!traceType.isCategorical()) {
                getChart().setXAxis(new DiscreteAxis(true, true));
            } else if (traceType.isCategorical()) {
                getChart().setXAxis(new DiscreteAxis(trace.getCategoryLabelMap(), true, true));
            } else {
                throw new RuntimeException("Trace type is not recognized: " + traceType);
            }
        }
    }

    /**
     * set x labs using <code>setXAxisTitle</code> when x-axis allows multiple traces
     */
    protected void setXLabelMultipleTraces() {
        if (getTraceLists().length == 1) {
            getChartPanel().setXAxisTitle(getTraceLists()[0].getName());
        } else if (getTraceNames().size() == 1) {
            getChartPanel().setXAxisTitle(getTraceNames().get(0));
        } else {
            getChartPanel().setXAxisTitle("Multiple Traces");
        }
    }
    /**
     * set y labs using <code>setYAxisTitle</code> when y-axis allows multiple traces
     */
    protected void setYLabelMultipleTraces() {
        if (getTraceLists().length == 1) {
            getChartPanel().setYAxisTitle(getTraceLists()[0].getName());
        } else if (getTraceNames().size() == 1) {
            getChartPanel().setYAxisTitle(getTraceNames().get(0));
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
