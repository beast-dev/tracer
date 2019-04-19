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
import dr.app.gui.chart.JChart;
import dr.app.gui.chart.JChartPanel;
import dr.inference.trace.TraceList;
import dr.inference.trace.TraceType;
import jam.framework.Exportable;
import tracer.application.PanelUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A shared code for the panel that displays a plot of traces,
 * such as most part of toolbar, and the chart panel.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Walter Xie
 * @version $Id: DensityPanel.java,v 1.3 2006/11/29 09:54:30 rambaut Exp $
 */
public abstract class TraceChartPanel extends JPanel implements Exportable {

    protected enum LegendAlignment {
        NONE("None"),
        TOP("Top"), TOP_RIGHT("Top-Right"), RIGHT("Right"), BOTTOM_RIGHT("Bottom-Right"),
        BOTTOM("Bottom"), BOTTOM_LEFT("Bottom-Left"), LEFT("Left"), TOP_LEFT("Top-Left");

        LegendAlignment(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private String name;
    };

    protected enum ColourByOptions {
        COLOUR_BY_TRACE("Trace"),
        COLOUR_BY_FILE("Trace file"),
        COLOUR_BY_FILE_AND_TRACE("Trace and Trace File");

        ColourByOptions(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private String name;
    };

    public enum ShowType {
        BOX_AND_WHISKER("box and whisker"),
        VIOLIN("violin");

        ShowType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private String name;
    }

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
        LegendAlignment legendAlignment = LegendAlignment.NONE;
        ColourByOptions colourBy = ColourByOptions.COLOUR_BY_TRACE;
        Paint[] palette = RAINBOW;
        ShowType show = ShowType.BOX_AND_WHISKER;

        ColourManager cm;

        // this is only used in FrequencyPanel, put here just to reduce code
        int minimumBins = 50;

        public Settings() {

            // Create a palette with contrasting colors alternating
            int N = 4;
            palette = new Paint[N * 3];

            cm = new ColourManager(palette.length);

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

    protected class ColourManager {

        private HashMap<String, Integer> traceFileMap;
        private HashMap<String, Integer> statisticMap;
        private HashMap<MultiKeyString, Integer> traceStatisticMap;

        private ArrayList<String> traceFiles;

        private int paletteLength;

        private int traceFileCounter = 0;
        private int statisticCounter = 0;
        private int traceStatisticCounter = 0;

        public ColourManager(int paletteLength) {
            this.paletteLength = paletteLength;
            this.traceFiles = new ArrayList<String>();
            this.traceFileMap = new HashMap<String, Integer>();
            this.statisticMap = new HashMap<String, Integer>();
            this.traceStatisticMap = new HashMap<MultiKeyString, Integer>();
        }

        public void clear() {
            this.traceFileCounter = 0;
            this.statisticCounter = 0;
            this.traceStatisticCounter = 0;

            this.traceFiles.clear();

            this.traceFileMap.clear();
            this.statisticMap.clear();
            this.traceStatisticMap.clear();
        }

        public boolean containsTraceFile(String fileName) {
            return traceFiles.contains(fileName);
        }

        public int addTraceColour(String traceFileName, String traceName, ColourByOptions option) {
            if (!traceFiles.contains(traceFileName)) {
                traceFiles.add(traceFileName);
            }

            if (option == ColourByOptions.COLOUR_BY_FILE) {
                if (traceFileMap.containsKey(traceFileName)) {
                    return traceFileMap.get(traceFileName);
                } else {
                    int tmp = traceFileCounter;
                    traceFileMap.put(traceFileName, traceFileCounter);
                    traceFileCounter = (traceFileCounter+1)%paletteLength;
                    return tmp;
                }
            } else if (option == ColourByOptions.COLOUR_BY_TRACE) {
                if (statisticMap.containsKey(traceName)) {
                    return statisticMap.get(traceName);
                } else {
                    int tmp = statisticCounter;
                    statisticMap.put(traceName, statisticCounter);
                    statisticCounter = (statisticCounter+1)%paletteLength;
                    return tmp;
                }
            } else if (option == ColourByOptions.COLOUR_BY_FILE_AND_TRACE) {
                MultiKeyString keyString = new MultiKeyString(traceFileName, traceName);
                if (traceStatisticMap.containsKey(keyString)) {
                    return traceStatisticMap.get(keyString);
                } else {
                    int tmp = traceStatisticCounter;
                    traceStatisticMap.put(keyString, traceStatisticCounter);
                    traceStatisticCounter = (traceStatisticCounter+1)%paletteLength;
                    return tmp;
                }
            } else {
                throw new RuntimeException("Invalid trace coloring scheme.");
            }
        }

        private class MultiKeyString {

            private String keyOne;
            private String keyTwo;

            public MultiKeyString(String keyOne, String keyTwo) {
                this.keyOne = keyOne;
                this.keyTwo = keyTwo;
            }

            public String getKeyOne() {
                return this.keyOne;
            }

            public String getKeyTwo() {
                return this.keyTwo;
            }

            /**
             * override hash code behaviour to force a call to equals()
             */
            @Override
            public int hashCode() {
                return this.keyTwo.hashCode();
            }

            // check for String equality when comparing these objects
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                MultiKeyString castObject = (MultiKeyString)obj;
                if (castObject.getKeyOne().equals(keyOne) && castObject.getKeyTwo().equals(keyTwo)) {
                    return true;
                } else {
                    return false;
                }
            }

        }

    }

    //++++++ setup traces +++++++
    private TraceList[] traceLists = null;
    private java.util.List<String> traceNames = null;
    private final JFrame frame;

    private String message = null;

    /**
     * main panel
     */
    public TraceChartPanel(final JFrame frame) {
        this.frame = frame;

        setOpaque(false);

        setMinimumSize(new Dimension(300, 150));
        setLayout(new BorderLayout());
    }

    protected final void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {
        this.traceLists = traceLists;
        this.traceNames = traceNames;

        setupMainPanel();
    }

    protected final void setupMainPanel() {
        if (traceLists == null || traceLists[0] == null || traceNames == null || traceNames.size() == 0) {
            setMessage("No traces selected.");
        } else {
            setMessage("");
            setupTraces();
        }

        removeAll();
        if (message != null && message.length() > 0) {
            add(new JLabel(message), BorderLayout.CENTER);
            validate();
            repaint();
            return;
        }
        if (getTopToolBar() != null) {
            add(getTopToolBar(), BorderLayout.NORTH);
        }
        if (getToolBar() != null) {
            add(getToolBar(), BorderLayout.SOUTH);
        }
        add(getChartPanel(), BorderLayout.CENTER);

        validate();
        repaint();
    }

    protected abstract void setupTraces();

    protected int getTraceCount() {
        return getTraceLists().length * getTraceNames().size();
    }

//    protected Trace getTrace(int index) {
//        int i = index / getTraceNames().size();
//        int j = index % getTraceNames().size();
//
//        TraceList traceList = getTraceLists()[i];
//        return traceList.getTrace(traceList.getTraceIndex(getTraceNames().get(j)));
//    }

    protected abstract JChartPanel getChartPanel();

    protected abstract ChartSetupDialog getChartSetupDialog();

    protected abstract Settings getSettings();

    protected abstract JToolBar getToolBar();

    /**
     * override to provide a toolbar for the top of the panel
     * @return
     */
    protected JToolBar getTopToolBar() {
        return null;
    }

    public JFrame getFrame() {
        return frame;
    }

    public TraceList[] getTraceLists() {
        return traceLists;
    }

    public java.util.List<String> getTraceNames() {
        return traceNames;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Create and return a new JToolBar
     */
    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setOpaque(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolBar.setFloatable(false);
        return toolBar;
    }

    /**
     * Create and return a new setup button
     * @return
     */
    protected JButton createSetupButton() {
        JButton chartSetupButton = new JButton("Setup...");
        PanelUtils.setupComponent(chartSetupButton);
        chartSetupButton.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        showChartSetupDialog();
                    }
                }
        );
        return chartSetupButton;
    }

    protected void showChartSetupDialog() {
        ChartSetupDialog chartSetupDialog = getChartSetupDialog();

        chartSetupDialog.showDialog(getChartPanel().getChart());

        chartSetupDialog.applySettings(getChartPanel().getChart());

        validate();
        repaint();
    }

    /**
     * Create and return a bins combo and its label.
     * @returns the label but the combo can be accessed with .getLabelFor() method.
     */
    protected JLabel createBinsComboAndLabel() {
        JLabel labelBins = new JLabel("Bins:");
        final JComboBox binsCombo = new JComboBox(
                new Integer[]{10, 20, 50, 100, 200, 500, 1000});
        binsCombo.setFont(UIManager.getFont("SmallSystemFont"));
        binsCombo.setOpaque(false);
        labelBins.setFont(UIManager.getFont("SmallSystemFont"));
        labelBins.setLabelFor(binsCombo);

        binsCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSettings().minimumBins = (Integer) binsCombo.getSelectedItem();
                        setupMainPanel();
                    }
                }
        );
        return labelBins;
    }

    protected JLabel createShowComboAndLabel() {
        JLabel labelShow = new JLabel("Show:");
        final JComboBox showCombo = new JComboBox(ShowType.values());
        showCombo.setFont(UIManager.getFont("SmallSystemFont"));
        showCombo.setOpaque(false);
        labelShow.setFont(UIManager.getFont("SmallSystemFont"));
        labelShow.setLabelFor(showCombo);

        showCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSettings().show = (ShowType)showCombo.getSelectedItem();
                        setupMainPanel();
                    }
                }
        );
        return labelShow;
    }


    /**
     * Create legend combo and its label.
     * @returns the label but the combo can be accessed with .getLabelFor() method.
     */
    protected JLabel createLegendComboAndLabel() {
        final JComboBox legendCombo = new JComboBox(LegendAlignment.values());
        legendCombo.setFont(UIManager.getFont("SmallSystemFont"));
        legendCombo.setOpaque(false);

        JLabel label = new JLabel("Legend:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(legendCombo);

        legendCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSettings().legendAlignment = (LegendAlignment)legendCombo.getSelectedItem();
                        setupMainPanel();
                    }
                }
        );

        return label;
    }

    /**
     * Create colour by combo and its label.
     * @returns the label but the combo can be accessed with .getLabelFor() method.
     */
    protected JLabel createColourByComboAndLabel() {
        final JComboBox colourByCombo = new JComboBox(ColourByOptions.values());
        colourByCombo.setFont(UIManager.getFont("SmallSystemFont"));
        colourByCombo.setOpaque(false);

        JLabel label = new JLabel("Colour by:");
        label.setFont(UIManager.getFont("SmallSystemFont"));
        label.setLabelFor(colourByCombo);

        colourByCombo.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSettings().colourBy = (ColourByOptions)colourByCombo.getSelectedItem();
                        setupMainPanel();
                    }
                }
        );
        return label;
    }

    /**
     * set legend given <code>Settings</code> which includes legend position and colours.
     */
    protected void setLegend(final LegendAlignment legendAlignment) {
        final JChart chart = getChartPanel().getChart();
        switch (legendAlignment) {
            case NONE:
                break;
            case TOP:
                chart.setLegendAlignment(SwingConstants.NORTH);
                break;
            case TOP_RIGHT:
                chart.setLegendAlignment(SwingConstants.NORTH_EAST);
                break;
            case RIGHT:
                chart.setLegendAlignment(SwingConstants.EAST);
                break;
            case BOTTOM_RIGHT:
                chart.setLegendAlignment(SwingConstants.SOUTH_EAST);
                break;
            case BOTTOM:
                chart.setLegendAlignment(SwingConstants.SOUTH);
                break;
            case BOTTOM_LEFT:
                chart.setLegendAlignment(SwingConstants.SOUTH_WEST);
                break;
            case LEFT:
                chart.setLegendAlignment(SwingConstants.WEST);
                break;
            case TOP_LEFT:
                chart.setLegendAlignment(SwingConstants.NORTH_WEST);
                break;
        }
        chart.setShowLegend(legendAlignment != LegendAlignment.NONE);
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
