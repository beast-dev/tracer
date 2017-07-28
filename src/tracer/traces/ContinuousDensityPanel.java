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
import dr.inference.trace.TraceCorrelation;
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
    private static final int DEFAULT_KDE_BINS = 5000;

    private final JParallelChart violinChart;
    private final JChartPanel violinChartPanel;

    private final JChart densityChart;
    private final JChartPanel densityChartPanel;

    private enum Type {
        KDE,
        HISTOGRAM,
        VIOLIN
    }

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
//    private Map<String, Settings> settingsMap = new HashMap<String, Settings>();

    //+++++ private field +++++
    private JComboBox displayCombo = new JComboBox(
            new String[]{"KDE", "Histogram", "Violin"}
    );

//    private JCheckBox kdeCheckBox = new JCheckBox("KDE");
//    private JButton kdeSetupButton = new JButton("Settings...");

    private JCheckBox relativeDensityCheckBox = new JCheckBox("Relative density");
    private JCheckBox solidCheckBox = new JCheckBox("Fill plot");

//    private TraceType traceType = null;

    /**
     * Creates new FrequencyPanel
     */
    public ContinuousDensityPanel(final JFrame frame) {
        super(frame);
        densityChart = new JChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        densityChartPanel = new JChartPanel(densityChart, "","","");

        violinChart = new JParallelChart(new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS));
        violinChartPanel = new JChartPanel(violinChart, "","","");

        JToolBar toolBar = setupToolBar(frame);
        addMainPanel(toolBar);
    }

    protected JChartPanel getChartPanel() {
        if (currentSettings.type == Type.VIOLIN) {
            return violinChartPanel;
        } else {
            return densityChartPanel;
        }
    }

    protected JToolBar setupToolBar(final JFrame frame) {
        JToolBar toolBar = super.setupToolBar(frame, currentSettings);

        addBins(toolBar);
        binsCombo.setSelectedItem(currentSettings.minimumBins);

        JLabel label = new JLabel("Display:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(displayCombo);
        toolBar.add(label);
        displayCombo.setFont(UIManager.getFont("SmallSystemFont"));
        displayCombo.setOpaque(false);
        toolBar.add(displayCombo);

        addLegend(toolBar);

        // +++++++ Listener ++++++++
        binsCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        currentSettings.minimumBins = (Integer) binsCombo.getSelectedItem();
                        setupTraces();
                    }
                }
        );

        chartSetupButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        if (currentSettings.chartSetupDialog == null) {
                            currentSettings.chartSetupDialog = new ChartSetupDialog(frame, true, false,
                                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
                        }

                        currentSettings.chartSetupDialog.showDialog(getChartPanel().getChart());
                        validate();
                        repaint();
                    }
                }
        );

        legendCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        currentSettings.legendAlignment = legendCombo.getSelectedIndex();
                        setupTraces();
                    }
                }
        );

        colourByCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        currentSettings.colourBy = ColourByOptions.values()[colourByCombo.getSelectedIndex()];
                        setupTraces();
                    }
                }
        );

//        relativeDensityCheckBox.addItemListener(
//                new java.awt.event.ItemListener() {
//                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
//                        currentSettings.relativeDensity = relativeDensityCheckBox.isSelected();
//                        setupTraces();
//                    }
//                }
//        );
//
//        solidCheckBox.addItemListener(
//                new java.awt.event.ItemListener() {
//                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
//                        currentSettings.drawSolid = solidCheckBox.isSelected();
//                        setupTraces();
//                    }
//                }
//        );

        displayCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        switch (displayCombo.getSelectedIndex()) {
                            case 0: currentSettings.type = Type.KDE; break;
                            case 1: currentSettings.type = Type.HISTOGRAM; break;
                            case 2: currentSettings.type = Type.VIOLIN; break;
                            default: throw new RuntimeException("Unknown display type");
                        }
                        binsCombo.setEnabled(currentSettings.type == Type.HISTOGRAM);
                        setupTraces();
                    }
                }
        );

//        kdeCheckBox.addItemListener(
//                new java.awt.event.ItemListener() {
//                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
//                        currentSettings.showKDE = kdeCheckBox.isSelected();
//                        kdeSetupButton.setEnabled(currentSettings.showKDE);
//                        setupTraces();
//                    }
//                }
//        );
//        kdeSetupButton.addActionListener(
//                new java.awt.event.ActionListener() {
//                    public void actionPerformed(ActionEvent actionEvent) {
//                        if (currentSettings.kdeSetupDialog == null) {
//                            currentSettings.kdeSetupDialog = new KDESetupDialog(frame, true, false,
//                                    Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK, Axis.AT_ZERO, Axis.AT_MAJOR_TICK);
//                        }
//
//                        currentSettings.kdeSetupDialog.showDialog(densityChart);
//                        setupTraces();
//                    }
//                }
//        );

        return toolBar;
    }

    public void setTraces(TraceList[] traceLists, List<String> traceNames) {
        super.setTraces(traceLists, traceNames);

//        if (traceNames.size() > 0) {
//            // find the first settings for the one of the selected traces...
//            Settings settings = null;
//
//            for (String name : traceNames) {
//                settings = settingsMap.get(name);
//                if (settings != null) {
//                    break;
//                }
//            }
//            if (settings == null) {
//                // if none of the traces have settings yet, create and store one for the
//                // first selected trace
//                settings = new Settings();
//                settingsMap.put(traceNames.get(0), settings);
//            }
//            currentSettings = settings;
//        }

        displayCombo.setSelectedIndex(currentSettings.type == Type.KDE ? 0 : (currentSettings.type == Type.HISTOGRAM ? 1 : 2));
//        binsCombo.setEnabled(currentSettings.showHistogram);

        binsCombo.setSelectedItem(currentSettings.minimumBins);
        relativeDensityCheckBox.setSelected(currentSettings.relativeDensity);
        legendCombo.setSelectedIndex(currentSettings.legendAlignment);
        colourByCombo.setSelectedIndex(currentSettings.colourBy.ordinal());
//        kdeCheckBox.setSelected(currentSettings.showKDE);
//        kdeSetupButton.setEnabled(currentSettings.showKDE);


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
                            getChartPanel().getChart().removeAllPlots();

                            getChartPanel().setXAxisTitle("");
                            getChartPanel().setYAxisTitle("");
                            messageLabel.setText("Unable to display a mixture statistics types.");
                            return;
                        }
                        messageLabel.setText("");
                    }
                }
            }

            // only enable controls relevant to continuous densities...
            displayCombo.setEnabled(traceType == TraceType.REAL);
            relativeDensityCheckBox.setEnabled(traceType == TraceType.REAL);
            labelBins.setEnabled(traceType == TraceType.REAL);
            binsCombo.setEnabled(traceType == TraceType.REAL && currentSettings.type == Type.HISTOGRAM);
//        kdeCheckBox.setEnabled(traceType == TraceFactory.TraceType.DOUBLE);
//        kdeSetupButton.setEnabled(traceType == TraceFactory.TraceType.DOUBLE);
        }

        setupTraces();
    }

    protected Plot setupDensityPlot(List values, TraceCorrelation td) {
        NumericalDensityPlot plot = new NumericalDensityPlot(values, currentSettings.minimumBins, td);
        return plot;
    }

    protected Plot setupKDEPlot(List values, TraceCorrelation td) {
        Plot plot = new KDENumericalDensityPlot(values, DEFAULT_KDE_BINS, td);
        return plot;
    }

    protected Plot setupViolinPlot(double xOffset, List values, TraceCorrelation td) {
        Plot plot = new ViolinPlot(xOffset, 0.8, values, DEFAULT_KDE_BINS, td);
        return plot;
    }


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
                    assert traceType.isContinuous();

                    switch (currentSettings.type) {
                        case KDE:
                            plot = setupKDEPlot(values, td);
                            plot.setName(name + " KDE");
                            if (tl instanceof CombinedTraces) {
                                plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[i]);
                            } else {
                                plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[i]);
                            }
                            break;
                        case HISTOGRAM:
                            plot = setupDensityPlot(values, td);
                            ((NumericalDensityPlot) plot).setRelativeDensity(currentSettings.relativeDensity);
                            break;
                        case VIOLIN:
                            plot = setupViolinPlot(i + 1, values, td);
                            plot.setName(name + " KDE");
                            if (tl instanceof CombinedTraces) {
                                plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[i]);
                            } else {
                                plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[i]);
                            }
                            break;
                    }

                    if (plot != null) {
                        plot.setName(name);
                        if (tl instanceof CombinedTraces) {
                            plot.setLineStyle(new BasicStroke(2.0f), currentSettings.palette[i]);
                        } else {
                            plot.setLineStyle(new BasicStroke(1.0f), currentSettings.palette[i]);
                        }

                        getChartPanel().getChart().addPlot(plot);
                    }

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
        if (currentSettings.type == Type.VIOLIN) {
            setYLabel("Value");
        } else {
            setYLabel(traceType, new String[]{"Density", "Probability"});
        }
        setLegend(currentSettings);
        setChartSetupDialog(currentSettings);

        validate();
        repaint();
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
