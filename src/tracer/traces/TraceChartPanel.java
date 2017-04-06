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

import dr.app.gui.chart.ChartSetupDialog;
import dr.app.gui.chart.DiscreteJChart;
import dr.app.gui.chart.JChart;
import dr.app.gui.chart.JChartPanel;
import dr.inference.trace.TraceDistribution;
import dr.inference.trace.TraceType;
import jam.framework.Exportable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A shared code for the panel that displays a plot of traces,
 * such as most part of toolbar, and the chart panel.
 * {@link #setupToolBar(JFrame) abstract setupToolBar} makes sure
 * the customized toolbar is implemented in children classes.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Walter Xie
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public abstract class TraceChartPanel extends JPanel implements Exportable {

    protected enum ColourByOptions {
        COLOUR_BY_TRACE,
        COLOUR_BY_FILE,
        COLOUR_BY_ALL
    };

    protected static final Paint[] paints = new Paint[]{
            Color.BLACK,
            new Color(64, 35, 225),
            new Color(229, 35, 60),
            new Color(255, 174, 34),
            new Color(86, 255, 34),
            new Color(35, 141, 148),
            new Color(146, 35, 142),
            new Color(255, 90, 34),
            new Color(239, 255, 34),
            Color.DARK_GRAY
    };

    protected class Settings {
        // shared settings
        ChartSetupDialog chartSetupDialog = null;
        int legendAlignment = 0;
        ColourByOptions colourBy = ColourByOptions.COLOUR_BY_TRACE;

        // this is only used in FrequencyPanel, put here just to reduce code
        int minimumBins = 50;
    }

    /**
     * <code>traceChart</code> has to be declared either as DiscreteJChart or JIntervalsChart
     */
    protected JChart traceChart;
    protected JChartPanel chartPanel;

    protected JLabel labelBins;
    protected JComboBox binsCombo = new JComboBox(
            new Integer[]{10, 20, 50, 100, 200, 500, 1000});

    protected JComboBox legendCombo = new JComboBox(
            new String[]{"None", "Top-Left", "Top", "Top-Right", "Left",
                    "Right", "Bottom-Left", "Bottom", "Bottom-Right"}
    );
    protected JComboBox colourByCombo = new JComboBox(
            new String[]{"Trace", "Trace File", "All"}
    );
    protected JButton chartSetupButton = new JButton("Axes...");
    protected JLabel messageLabel = new JLabel("No data loaded");

    protected JCheckBox showValuesCheckBox = new JCheckBox("Show values on above chart");

    protected final JFrame frame;

    /**
     * main panel
     */
    public TraceChartPanel(final JFrame frame) {
        this.frame = frame;

        setOpaque(false);

        setMinimumSize(new Dimension(300, 150));
        setLayout(new BorderLayout());

        //JToolBar toolBar = setupToolBar(frame);
        //addMainPanel(toolBar);
    }



    /**
     * encapsulate <code>traceChart</code>
     *
     * @return {@see DiscreteJChart} in {@see tracer.traces.JointDensityPanel},
     *         or {@see JIntervalsChart} in {@see tracer.traces.FrequencyPanel} and {@see tracer.traces.DensityPanel},
     *         or {@see JTraceChart} in {@see tracer.traces.RawTracePanel}
     */
    protected abstract JChart getTraceChart();

    /**
     * create panel container to display <code>traceChart</code>.
     */
    protected void initJChartPanel(String xAxisTitle, String yAxisTitle){
        chartPanel = new JChartPanel(getTraceChart(), null, xAxisTitle, yAxisTitle);
    }

    /**
     * add components to main panel
     * @param toolBar get from {@link #setupToolBar(JFrame, Settings) setupToolBar}
     */
    protected void addMainPanel(JToolBar toolBar, boolean addMessageLabel) {
        if (addMessageLabel)
            add(messageLabel, BorderLayout.NORTH);
        add(toolBar, BorderLayout.SOUTH);
        if (chartPanel==null)
            throw new IllegalArgumentException("chartPanel is null, please use initJChartPanel(JChart traceChart) in constructor !");
        add(chartPanel, BorderLayout.CENTER);
    }

    protected void addMainPanel(JToolBar toolBar) {
        addMainPanel(toolBar, true);
    }

    /**
     * Used in the constructor of the child class to customize toolbar
     *
     * @param frame
     * @return
     */
    protected abstract JToolBar setupToolBar(final JFrame frame);

    /**
     * Create {@see JToolBar} toolBar and add axes components
     * @param frame
     * @param currentSettings
     * @return
     */
    protected JToolBar setupToolBar(final JFrame frame, final Settings currentSettings) {
        JToolBar toolBar = new JToolBar();
        toolBar.setOpaque(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolBar.setFloatable(false);

        chartSetupButton.putClientProperty(
                "Quaqua.Button.style", "placard"
        );
        chartSetupButton.setFont(UIManager.getFont("SmallSystemFont"));
        toolBar.add(chartSetupButton);

        return toolBar;
    }

    /**
     * Add bins components to {@see JToolBar} toolBar,
     * but their listeners have to be added in the child class
     * @param toolBar
     */
    protected void addBins(final JToolBar toolBar) {
        binsCombo.setFont(UIManager.getFont("SmallSystemFont"));
        binsCombo.setOpaque(false);
        labelBins = new JLabel("Bins:");
        labelBins.setFont(UIManager.getFont("SmallSystemFont"));
        labelBins.setLabelFor(binsCombo);
        toolBar.add(labelBins);
        toolBar.add(binsCombo);
    }

    /**
     * Add legend components to {@see JToolBar} toolBar
     * between two <code>JToolBar.Separator</code>,
     * but their listeners have to be added in the child class.
     *
     * @param toolBar
     */
    protected void addLegend(final JToolBar toolBar) {
        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));

        JLabel label = new JLabel("Legend:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(legendCombo);
        toolBar.add(label);
        legendCombo.setFont(UIManager.getFont("SmallSystemFont"));
        legendCombo.setOpaque(false);
        toolBar.add(legendCombo);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));
        label = new JLabel("Colour by:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(colourByCombo);
        toolBar.add(label);
        colourByCombo.setFont(UIManager.getFont("SmallSystemFont"));
        colourByCombo.setOpaque(false);
        toolBar.add(colourByCombo);

        toolBar.add(new JToolBar.Separator(new Dimension(8, 8)));
    }

    //++++++ setup chart +++++++

    /**
     * set legend given <code>Settings</code> which includes legend position and colours.
     *
     * @param currentSettings
     */
    protected void setLegend(final Settings currentSettings) {
        switch (currentSettings.legendAlignment) {
            case 0:
                break;
            case 1:
                getTraceChart().setLegendAlignment(SwingConstants.NORTH_WEST);
                break;
            case 2:
                getTraceChart().setLegendAlignment(SwingConstants.NORTH);
                break;
            case 3:
                getTraceChart().setLegendAlignment(SwingConstants.NORTH_EAST);
                break;
            case 4:
                getTraceChart().setLegendAlignment(SwingConstants.WEST);
                break;
            case 5:
                getTraceChart().setLegendAlignment(SwingConstants.EAST);
                break;
            case 6:
                getTraceChart().setLegendAlignment(SwingConstants.SOUTH_WEST);
                break;
            case 7:
                getTraceChart().setLegendAlignment(SwingConstants.SOUTH);
                break;
            case 8:
                getTraceChart().setLegendAlignment(SwingConstants.SOUTH_EAST);
                break;
        }
        getTraceChart().setShowLegend(currentSettings.legendAlignment != 0);
    }

    /**
     * set <code>ChartSetupDialog</code> about axes scales.
     *
     * @param currentSettings
     */
    protected void setChartSetupDialog(Settings currentSettings) {
        if (currentSettings.chartSetupDialog != null) {
            currentSettings.chartSetupDialog.applySettings(getTraceChart());
        }
    }

    /**
     *  {@link dr.app.gui.chart.DiscreteJChart#setXAxis(boolean, Map<Integer, String>) setXAxis},
     *  used in {@see tracer.traces.FrequencyPanel} and {@see tracer.traces.DensityPanel}
     *
     * @param td
     */
    protected void setXAxis(TraceDistribution td) {
        if (td != null) {
            TraceType traceType = td.getTraceType();

            if (!(getTraceChart() instanceof DiscreteJChart))
                throw new RuntimeException("traceChart has to be instanceof DiscreteJChart, " +
                        "using setXAxis(TraceType traceType, Map<Integer, String> categoryDataMap) !");

            if (!traceType.isCategorical()) {
                ((DiscreteJChart) getTraceChart()).setXAxis(traceType.isIntegerOrBinary(),
                        new HashMap<Integer, String>());

            } else if (traceType == TraceType.CATEGORICAL) {
                ((DiscreteJChart) getTraceChart()).setXAxis(false, td.getIndexMap());

            } else {
                throw new RuntimeException("Trace type is not recognized: " + traceType);
            }
        }
    }

    protected void setXLab(String xLab) {
        chartPanel.setXAxisTitle(xLab);
    }

    protected void setYLab(String yLab) {
        chartPanel.setYAxisTitle(yLab);
    }

    /**
     * {@link dr.app.gui.chart.JChartPanel#setYAxisTitle(String) setYAxisTitle},
     * used in {@see tracer.traces.FrequencyPanel} and {@see tracer.traces.DensityPanel}
     *
     * @param traceType
     * @param yLabs
     */
    protected void setYLab(TraceType traceType, String[] yLabs) {
        if (traceType != null) {
            if (yLabs.length != 2)
                throw new IllegalArgumentException("Y labs array must have 2 element !");

            if (traceType.isContinuous()) {
                chartPanel.setYAxisTitle(yLabs[0]);

            } else if (traceType.isIntegerOrBinary()) {
                chartPanel.setYAxisTitle(yLabs[1]);

            } else if (traceType.isCategorical()) {
                chartPanel.setYAxisTitle(yLabs[1]);

            } else {
                throw new RuntimeException("Trace type is not recognized: " + traceType);
            }
        }
    }

    public JComponent getExportableComponent() {
        return chartPanel;
    }

}
