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
public class DensityPanel extends NTracesChartPanel {
    private static final int DEFAULT_KDE_BINS = 5000;

    private class Settings extends TraceChartPanel.Settings {
//        ChartSetupDialog chartSetupDialog = null;
//        KDESetupDialog kdeSetupDialog = null;
        int minimumBins = 100;
        boolean showKDE = true;
        boolean showHistogram = false;
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
            new String[]{"KDE", "Histogram", "Both"}
    );

//    private JCheckBox kdeCheckBox = new JCheckBox("KDE");
//    private JButton kdeSetupButton = new JButton("Settings...");

    private JCheckBox relativeDensityCheckBox = new JCheckBox("Relative density");
    private JCheckBox solidCheckBox = new JCheckBox("Fill plot");

//    private TraceType traceType = null;

    /**
     * Creates new FrequencyPanel
     */
    public DensityPanel(final JFrame frame) {
        super(frame);
        traceChart = new DiscreteJChart(
                new LinearAxis(Axis.AT_MAJOR_TICK_PLUS, Axis.AT_MAJOR_TICK_PLUS), new LinearAxis());
        initJChartPanel("", ""); // xAxisTitle, yAxisTitle
        JToolBar toolBar = setupToolBar(frame);
        addMainPanel(toolBar);
    }

    protected DiscreteJChart getTraceChart() {
        return (DiscreteJChart) traceChart;
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

        // KDE's don' do this at present so just taking up space on the toolbar...
//        relativeDensityCheckBox.setOpaque(false);
//        relativeDensityCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
//        toolBar.add(relativeDensityCheckBox);

        // Probably don't need this as an option - takes up space and
        // solid (translucent) plots look cool...
//		solidCheckBox.setOpaque(false);
//		solidCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
//		solidCheckBox.setSelected(true);
//		toolBar.add(solidCheckBox);

        addLegend(toolBar);

//        kdeCheckBox.setFont(UIManager.getFont("SmallSystemFont"));
//        toolBar.add(kdeCheckBox);
//
//        kdeSetupButton.putClientProperty(
//                "Quaqua.Button.style", "placard"
//        );
//        kdeSetupButton.setFont(UIManager.getFont("SmallSystemFont"));
//        toolBar.add(kdeSetupButton);
//
//        kdeSetupButton.setEnabled(kdeCheckBox.isSelected());


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

                        currentSettings.chartSetupDialog.showDialog(getTraceChart());
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
                        currentSettings.showHistogram = displayCombo.getSelectedIndex() >= 1;
                        currentSettings.showKDE = displayCombo.getSelectedIndex() != 1;

                        binsCombo.setEnabled(currentSettings.showHistogram);
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

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
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

        displayCombo.setSelectedIndex(currentSettings.showHistogram && currentSettings.showKDE ? 2 : (currentSettings.showKDE ? 0 : 1));
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
                            getTraceChart().removeAllPlots();

                            chartPanel.setXAxisTitle("");
                            chartPanel.setYAxisTitle("");
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
            binsCombo.setEnabled(traceType == TraceType.REAL && currentSettings.showHistogram);
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

//    protected Plot setupIntegerPlot(List values, TraceType type, TraceCorrelation td, int barCount, int barId) {
//        CategoryDensityPlot plot = new CategoryDensityPlot(values, -1, td, barCount, barId);
//        return plot;
//    }
//
//    protected Plot setupCategoryPlot(List values, TraceCorrelation td, Map<Integer, String> categoryDataMap, int barCount, int barId) {
//        List<Double> intData = getIndexOfCategoricalValues(values, td, categoryDataMap);
//        CategoryDensityPlot plot = new CategoryDensityPlot(intData, -1, td, barCount, barId);
//        return plot;
//    }


    protected void setupTraces() {
        // return if no traces selected
        if (!rmAllPlots(false)) return;

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

                    // set traceType here to avoid Exception from setYLab
                    traceType = trace.getTraceType();
                    if (traceType.isContinuous()) {
                        if (currentSettings.showHistogram) {
                            plot = setupDensityPlot(values, td);
                            ((NumericalDensityPlot)plot).setRelativeDensity(currentSettings.relativeDensity);
                            ((NumericalDensityPlot)plot).setPointsOnly(currentSettings.showKDE);
                        } else {
                            plot = null;
                        }

                        if (currentSettings.showKDE) {
                            if (plot != null) {
                                ((NumericalDensityPlot)plot).setSolid(false);
                                ((NumericalDensityPlot)plot).setLineStroke(null);
                                ((NumericalDensityPlot)plot).setMarkStyle(Plot.POINT_MARK, 1, new BasicStroke(0.5f),
                                        Color.black, Color.black);
                            }

                            Plot plot2 = setupKDEPlot(values, td);
                            plot2.setName(name + " KDE");
                            if (tl instanceof CombinedTraces) {
                                plot2.setLineStyle(new BasicStroke(2.0f), paints[i]);
                            } else {
                                plot2.setLineStyle(new BasicStroke(1.0f), paints[i]);
                            }
                            traceChart.addPlot(plot2);
                        }

                    } else if (traceType.isOrdinal()) {

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
                            plot.setLineStyle(new BasicStroke(2.0f), paints[i]);
                        } else {
                            plot.setLineStyle(new BasicStroke(1.0f), paints[i]);
                        }

                        traceChart.addPlot(plot);
                    }
                    // change x axis to DiscreteAxis or LinearAxis according TraceType
                    setXAxis(td);

                    // colourBy
                    if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE || currentSettings.colourBy == ColourByOptions.COLOUR_BY_ALL) {
                        i++;
                    }
                    if (i == paints.length) i = 0;
                }
            }
            if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_FILE) {
                i++;
            } else if (currentSettings.colourBy == ColourByOptions.COLOUR_BY_TRACE) {
                i = 0;
            }
            if (i == paints.length) i = 0;
        }

        setXLabMultiTraces();
        setYLab(traceType, new String[]{"Density", "Probability"});
//        setColours(currentSettings);
        setLegend(currentSettings);
        setChartSetupDialog(currentSettings);

        validate();
        repaint();
    }


    public String toString() {
        if (traceChart.getPlotCount() == 0) {
            return "no plot available";
        }

        StringBuffer buffer = new StringBuffer();

        Plot plot = traceChart.getPlot(0);
        Variate xData = plot.getXData();

        buffer.append(chartPanel.getXAxisTitle());
        for (int i = 0; i < traceChart.getPlotCount(); i++) {
            plot = traceChart.getPlot(i);
            buffer.append("\t");
            buffer.append(plot.getName());
        }
        buffer.append("\n");

        for (int i = 0; i < xData.getCount(); i++) {
            buffer.append(String.valueOf(xData.get(i)));
            for (int j = 0; j < traceChart.getPlotCount(); j++) {
                plot = traceChart.getPlot(j);
                Variate yData = plot.getYData();
                buffer.append("\t");
                buffer.append(String.valueOf(yData.get(i)));
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }

}
