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
import dr.inference.trace.Trace;
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;
import dr.stats.Variate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * A panel that displays density plots of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public class ContinuousDensityPanel extends TraceChartPanel {

    private enum Type {
        KDE("KDE"),
        HISTOGRAM("Histogram"),
        VIOLIN("Violin"),
        BOX_AND_WHISKER("Box and whisker");

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private final String name;
    }

    private final JToolBar topToolBar;

    private final JChart kdeChart;
    private final JChartPanel kdeChartPanel;
    private final JToolBar kdeToolBar;

    private final JChart histogramChart;
    private final JChartPanel histogramChartPanel;
    private final JToolBar histogramToolBar;

    private final ChartSetupDialog densityChartSetupDialog;

    private final JParallelChart violinChart;
    private final JChartPanel violinChartPanel;
    private final JToolBar violinToolBar;
    private final ChartSetupDialog violinChartSetupDialog;

    private final JComboBox displayCombo = new JComboBox( Type.values() );

    private class Settings extends TraceChartPanel.Settings {
        //        ChartSetupDialog chartSetupDialog = null;
//        KDESetupDialog kdeSetupDialog = null;
        int minimumBins = 100;
        Type type = Type.KDE;
        boolean drawSolid = true;
        boolean relativeDensity = false;
        int barCount = 0;
//        int legendAlignment = 0;
//        ColourByOptions colourBy = ColourByOptions.COLOUR_BY_TRACE;
    }

    private Settings currentSettings = new Settings();


    /**
     * Creates new FrequencyPanel
     */
    public ContinuousDensityPanel(final JFrame frame) {
        super(frame);

        setOpaque(false);

        kdeChart = new JChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        kdeChartPanel = new JChartPanel(kdeChart, "","","");
        kdeToolBar = createToolBar(Type.KDE, currentSettings);
        densityChartSetupDialog = new ChartSetupDialog(frame, true, false, true, false,
                Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);

        histogramChart = new JChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        histogramChartPanel = new JChartPanel(histogramChart, "","","");
        histogramToolBar = createToolBar(Type.HISTOGRAM, currentSettings);

        violinChart = new JParallelChart(false, new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS));
        violinChartPanel = new JChartPanel(violinChart, "","","");
        violinToolBar = createToolBar(Type.VIOLIN, currentSettings);
        violinChartSetupDialog = new ChartSetupDialog(frame, false, true, false, true,
                Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK);

        topToolBar = createTopToolBar();
    }

    protected JToolBar getToolBar() {
        switch (currentSettings.type) {
            case KDE:
                return kdeToolBar;
            case HISTOGRAM:
                return histogramToolBar;
            case VIOLIN:
                return violinToolBar;
            case BOX_AND_WHISKER:
                return violinToolBar;
            default:
                throw new IllegalArgumentException("Unknown chart type");
        }
    }

    protected JToolBar getTopToolBar() {
        return topToolBar;
    }


    protected JChartPanel getChartPanel() {
        switch (currentSettings.type) {
            case KDE:
                return kdeChartPanel;
            case HISTOGRAM:
                return histogramChartPanel;
            case VIOLIN:
            case BOX_AND_WHISKER:
                return violinChartPanel;
            default:
                throw new IllegalArgumentException("Unknown chart type");
        }
    }

    private JToolBar createTopToolBar() {
        JToolBar topToolBar = createToolBar();

        JLabel label = new JLabel("Display:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(displayCombo);
        topToolBar.add(label);
        displayCombo.setFont(UIManager.getFont("SmallSystemFont"));
        displayCombo.setOpaque(false);
        topToolBar.add(displayCombo);

        displayCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        currentSettings.type = (Type)displayCombo.getSelectedItem();
                        setupMainPanel();
                    }
                }
        );
        return topToolBar;
    }

    private JToolBar createToolBar(Type type, Settings settings) {
        JToolBar toolBar = super.createToolBar();

        toolBar.add(createSetupButton());

        if (type == Type.HISTOGRAM) {
            toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

            JLabel label = createBinsComboAndLabel();
            toolBar.add(label);
            toolBar.add(label.getLabelFor());

            ((JComboBox)label.getLabelFor()).setSelectedItem(settings.minimumBins);
        }

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        JLabel label = createLegendComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.legendAlignment);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        label = createColourByComboAndLabel();
        toolBar.add(label);
        toolBar.add(label.getLabelFor());
        ((JComboBox)label.getLabelFor()).setSelectedItem(settings.colourBy.ordinal());

        return toolBar;
    }

    @Override
    protected ChartSetupDialog getChartSetupDialog() {
        switch (currentSettings.type) {
            case KDE:
            case HISTOGRAM:
                return densityChartSetupDialog;
            case VIOLIN:
            case BOX_AND_WHISKER:
                return violinChartSetupDialog;
            default:
                throw new IllegalArgumentException("Unknown chart type");
        }
    }

    @Override
    protected TraceChartPanel.Settings getSettings() {
        return currentSettings;
    }

    protected Plot createHistogramPlot(List values) {
        return new NumericalDensityPlot(values, getSettings().minimumBins);
    }

    protected Plot createKDEPlot(List values) {
        return new KDENumericalDensityPlot(values);
    }

    protected Plot createViolinPlot(List values, double lower, double upper) {
        return new ViolinPlot(true, 0.8, lower, upper, false, values);
    }

    protected Plot createBoxPlot(double lower, double upper, double lowerTail, double upperTail, double mean) {
        return new BoxPlot(true, 0.8, lower, upper, lowerTail, upperTail, mean);
    }

    protected void setupTraces() {

        TraceType traceType = null;

        displayCombo.setSelectedItem(currentSettings.type);

        getChartPanel().getChart().removeAllPlots();

        TraceList[] traceLists = getTraceLists();
        for (int i = 0; i < traceLists.length; i++) {
            TraceList tl = traceLists[i];

            for (String traceName : getTraceNames()) {
                int traceIndex = tl.getTraceIndex(traceName);
                Trace trace = tl.getTrace(traceIndex);
                Plot plot = null;

                if (trace != null) {
                    String name = tl.getTraceName(traceIndex);
                    if (traceLists.length > 1)
                        name = i + "-" + name;

                    List values = tl.getValues(traceIndex);

                    if (traceType != null && trace.getTraceType() != traceType) {
                        setMessage("Traces must be of the same type to visualize here.");
                    }

                    // set traceType here to avoid Exception from setYLabel
                    traceType = trace.getTraceType();

                    switch (currentSettings.type) {
                        case KDE:
                            plot = createKDEPlot(values);
                            break;
                        case HISTOGRAM:
                            plot = createHistogramPlot(values);
                            ((NumericalDensityPlot) plot).setRelativeDensity(currentSettings.relativeDensity);
                            break;
                        case VIOLIN: {
                            double lower = trace.getTraceStatistics().getLowerHPD();
                            double upper = trace.getTraceStatistics().getUpperHPD();
                            plot = createViolinPlot(values, lower, upper);
                            break;
                        }
                        case BOX_AND_WHISKER: {
                            double lower = trace.getTraceStatistics().getLowerHPD();
                            double upper = trace.getTraceStatistics().getUpperHPD();
                            double lowerTail = trace.getTraceStatistics().getMinimum();
                            double upperTail = trace.getTraceStatistics().getMaximum();
                            double mean = trace.getTraceStatistics().getMean();
                            plot = createBoxPlot(lower, upper, lowerTail, upperTail, mean);
                            break;
                        }
                    }

                    if (plot != null) {
                        plot.setName(name);
                        int selectedColour = currentSettings.cm.addTraceColour(tl.getFullName(), name, currentSettings.colourBy);
                        if (tl instanceof CombinedTraces) {
                            plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[selectedColour]);
                        } else {
                            plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[selectedColour]);
                        }
                        if (plot instanceof BoxPlot) {
                            ((BoxPlot)plot).setMeanLineStyle(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), currentSettings.palette[selectedColour]);
                        }

                        getChartPanel().getChart().addPlot(plot);
                    }

                }
            }
        }

        removeAll();
        add(getChartPanel(), BorderLayout.CENTER);
        add(getToolBar(), BorderLayout.SOUTH);

        setXLabelMultipleTraces();
        if (currentSettings.type == Type.VIOLIN || currentSettings.type == Type.BOX_AND_WHISKER) {
            setYLabel("Value");
        } else {
            setYLabel(traceType, new String[]{"Density", "Probability"});
        }
        setLegend(currentSettings.legendAlignment);

        validate();
        repaint();
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
        JChart chart = getChartPanel().getChart();

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
