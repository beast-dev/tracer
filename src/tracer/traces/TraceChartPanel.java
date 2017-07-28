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
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;
import jam.framework.Exportable;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.util.*;

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

    protected static final Paint[] LEGACY_PAINTS = new Paint[]{
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

    protected static final Paint[] COLORBREWER_PAIRED = new Paint[]{
            new Color(0xa6cee3),
            new Color(0x1f78b4),
            new Color(0xb2df8a),
            new Color(0x33a02c),
            new Color(0xfb9a99),
            new Color(0xe31a1c),
            new Color(0xfdbf6f),
            new Color(0xff7f00),
            new Color(0xcab2d6),
            new Color(0x6a3d9a),
            new Color(0xffff99),
            new Color(0xb15928)
    };

    protected static final Paint[] COLORBREWER_DARK2 = new Paint[]{
            new Color(0x1b9e77),
            new Color(0xd95f02),
            new Color(0x7570b3),
            new Color(0xe7298a),
            new Color(0x66a61e),
            new Color(0xe6ab02),
            new Color(0xa6761d),
            new Color(0x666666)
    };

    protected static final Paint[] RAINBOW = new Paint[]{
            new Color(0x46a4d5),
            new Color(0x3cbeb7),
            new Color(0xb0be36),
            new Color(0xfdcb42),
            new Color(0xf7a139),
            new Color(0xf4622e),
            new Color(0xd93a3d),
            new Color(0xec667c),
            new Color(0xa267d6)
    };

    protected static final Paint[] RAINBOW_2 = new Paint[]{
            new Color(0x394958),
            new Color(0x5b9586),
            new Color(0x6f955b),
            new Color(0xdfbd5d),
            new Color(0xd17e53),
            new Color(0xc25553),
            new Color(0x955b6e)
    };

    protected class Settings {
        // shared settings
        ChartSetupDialog chartSetupDialog = null;
        int legendAlignment = 0;
        ColourByOptions colourBy = ColourByOptions.COLOUR_BY_TRACE;
        Paint[] palette = RAINBOW;

        // this is only used in FrequencyPanel, put here just to reduce code
        int minimumBins = 50;

        public Settings() {

            // Create a palette with contrasting colors alternating
            int N = 4;
            palette = new Paint[N * 3];

            float cycle1 = 0.666F;
            float cycle2 = 0.333F;
            float cycle3 = 0.0F;
            float increment = 0.466F / N;

            for (int i = 0; i < palette.length; i += 3) {
                palette[i] = new Color(Color.HSBtoRGB(cycle1, 0.7F, 0.7F));
                palette[i+1] = new Color(Color.HSBtoRGB(cycle2, 0.7F, 0.7F));
                palette[i+2] = new Color(Color.HSBtoRGB(cycle3, 0.7F, 0.7F));

                cycle1 += increment;
                if (cycle1 > 1.0) { cycle1 -= 1.0F; }
                cycle2 += increment;
                if (cycle2 > 1.0) { cycle2 -= 1.0F; }
                cycle3 += increment;
                if (cycle3 > 1.0) { cycle3 -= 1.0F; }
            }
        }
    }

    //++++++ setup traces +++++++
    protected TraceList[] traceLists = null;
    protected java.util.List<String> traceNames = null;

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

    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        this.traceLists = traceLists;
        this.traceNames = traceNames;
    };


    protected abstract JChartPanel getChartPanel();

    /**
     * add components to main panel
     * @param toolBar get from {@link #setupToolBar(JFrame, Settings) setupToolBar}
     */
    protected void addMainPanel(JToolBar toolBar, boolean addMessageLabel) {
        if (addMessageLabel) {
            add(messageLabel, BorderLayout.NORTH);
        }
        add(toolBar, BorderLayout.SOUTH);
        add(getChartPanel(), BorderLayout.CENTER);
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
        JChart chart = getChartPanel().getChart();
        switch (currentSettings.legendAlignment) {
            case 0:
                break;
            case 1:
                chart.setLegendAlignment(SwingConstants.NORTH_WEST);
                break;
            case 2:
                chart.setLegendAlignment(SwingConstants.NORTH);
                break;
            case 3:
                chart.setLegendAlignment(SwingConstants.NORTH_EAST);
                break;
            case 4:
                chart.setLegendAlignment(SwingConstants.WEST);
                break;
            case 5:
                chart.setLegendAlignment(SwingConstants.EAST);
                break;
            case 6:
                chart.setLegendAlignment(SwingConstants.SOUTH_WEST);
                break;
            case 7:
                chart.setLegendAlignment(SwingConstants.SOUTH);
                break;
            case 8:
                chart.setLegendAlignment(SwingConstants.SOUTH_EAST);
                break;
        }
        chart.setShowLegend(currentSettings.legendAlignment != 0);
    }

    /**
     * set <code>ChartSetupDialog</code> about axes scales.
     *
     * @param currentSettings
     */
    protected void setChartSetupDialog(Settings currentSettings) {
        if (currentSettings.chartSetupDialog != null) {
            currentSettings.chartSetupDialog.applySettings(getChartPanel().getChart());
        }
    }

    protected void setXLabel(String xLabel) {
        getChartPanel().setXAxisTitle(xLabel);
    }

    protected void setYLabel(String yLabel) {
        getChartPanel().setYAxisTitle(yLabel);
    }

    /**
     * {@link dr.app.gui.chart.JChartPanel#setYAxisTitle(String) setYAxisTitle},
     * used in {@see tracer.traces.FrequencyPanel} and {@see tracer.traces.DensityPanel}
     *
     * @param traceType
     * @param yLabels
     */
    protected void setYLabel(TraceType traceType, String[] yLabels) {
        if (traceType != null) {
            if (yLabels.length != 2)
                throw new IllegalArgumentException("Y labs array must have 2 element !");

            if (traceType.isContinuous()) {
                getChartPanel().setYAxisTitle(yLabels[0]);

            } else if (traceType.isIntegerOrBinary()) {
                getChartPanel().setYAxisTitle(yLabels[1]);

            } else if (traceType.isCategorical()) {
                getChartPanel().setYAxisTitle(yLabels[1]);

            } else {
                throw new RuntimeException("Trace type is not recognized: " + traceType);
            }
        }
    }

    /**
     * If no traces selected, return false, else return true.
     * Usage: <code>if (!removeAllPlots()) return;</code>
     *
     * @return boolean
     */
    protected boolean removeAllPlots(boolean removeMessageLabel) {
        removeAllPlots();

        if (traceLists == null || traceNames == null || traceNames.size() == 0) {
            getChartPanel().setXAxisTitle("");
            getChartPanel().setYAxisTitle("");
            messageLabel.setText("No traces selected");
            add(messageLabel, BorderLayout.NORTH);
            return false;
        }

        if (removeMessageLabel) remove(messageLabel);
        return true;
    }

    /**
     * to overwrite it to <code>removeAllTraces()</code> in <code>RawTracePanel</code>
     */
    protected void removeAllPlots() {
        getChartPanel().getChart().removeAllPlots();

    }

    /**
     * set x labs using <code>setXAxisTitle</code> when x-axis allows multiple traces
     */
    protected void setXAxisLabel() {
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
    protected void setYAxisLabel() {
        if (traceLists.length == 1) {
            getChartPanel().setYAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            getChartPanel().setYAxisTitle(traceNames.get(0));
        } else {
            getChartPanel().setYAxisTitle("Multiple Traces");
        }
    }

    public JComponent getExportableComponent() {
        return getChartPanel();
    }

}