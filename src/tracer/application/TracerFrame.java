/*
 * TracerFrame.java
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

package tracer.application;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import dr.app.gui.FileDrop;
import dr.app.gui.chart.ChartRuntimeException;
import dr.app.gui.table.TableEditorStopper;
import dr.app.gui.util.LongTask;
import dr.app.util.OSType;
import dr.inference.trace.*;
import jam.framework.DocumentFrame;
import jam.panels.ActionPanel;
import jam.table.TableRenderer;
import jam.toolbar.Toolbar;
import tracer.analysis.*;
import tracer.traces.CombinedTraces;
import tracer.traces.FilterDialog;
import tracer.traces.FilterListPanel;
import tracer.traces.TracePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class TracerFrame extends DocumentFrame implements TracerFileMenuHandler, AnalysisMenuHandler {

    private final String[] columnToolTips = {null, null, null,
            "Trace Type: real(R), ordinal(O) or categorical(C)"};

    private TracePanel tracePanel = null;

    private JTable traceTable = null;
    private TraceTableModel traceTableModel = null;
    private JSplitPane splitPane1 = null;
    private JPanel topPanel = null;

    private JTable statisticTable = null;
    private StatisticTableModel statisticTableModel = null;

    private JScrollPane scrollPane1 = null;
    private JScrollPane scrollPane2 = null;

    private JLabel progressLabel;
    private JProgressBar progressBar;

    private final List<LogFileTraces> traceLists = new ArrayList<LogFileTraces>();
    private final List<TraceList> currentTraceLists = new ArrayList<TraceList>();
    // allTraceLists not used except deleting and adding log, traceLists did all jobs
//    private final List<TraceList> allTraceLists = new ArrayList<TraceList>();
    private CombinedTraces combinedTraces = null;

    private List<String> commonTraceNames = new ArrayList<String>();
    private boolean homogenousTraceFiles = true;

    private JButton realButton;
    private JButton integerButton;
    private JButton categoricalButton;
    private JButton temporalButton;

//    private final List<FilterListPanel> filterListPanelList = new ArrayList<FilterListPanel>();

    //    private final JComboBox filterCombo = new JComboBox(new String[]{"None"});
    private final JLabel filterStatus = new JLabel();
    String message = "";
    private int dividerLocation = -1;

    private DemographicDialog demographicDialog = null;
    private BayesianSkylineDialog bayesianSkylineDialog = null;
    private ExtendedBayesianSkylineDialog extendedBayesianSkylineDialog = null;
    private GMRFSkyrideDialog gmrfSkyrideDialog = null;
    private SkyGridDialog skyGridDialog = null;
    private TimeDensityDialog timeDensityDialog = null;
    private LineagesThroughTimeDialog lineagesThroughTimeDialog = null;
    private TraitThroughTimeDialog traitThroughTimeDialog = null;
    private NewTemporalAnalysisDialog createTemporalAnalysisDialog = null;

//    private FilterDialog filterDialog;

    public TracerFrame(String title) {
        super();

        setTitle(title);

        getOpenAction().setEnabled(false);
        getSaveAction().setEnabled(false);
        getSaveAsAction().setEnabled(false);

        getCutAction().setEnabled(false);
        getCopyAction().setEnabled(false);
        getPasteAction().setEnabled(false);
        getDeleteAction().setEnabled(false);
        getSelectAllAction().setEnabled(false);
        getFindAction().setEnabled(false);

        getZoomWindowAction().setEnabled(false);

        AbstractAction importAction = new AbstractAction("Import Trace File...") {
            public void actionPerformed(ActionEvent ae) {
                doImport();
            }
        };
        setImportAction(importAction);
        setExportAction(exportDataTableAction);

        setAnalysesEnabled(false);
    }

    public void initializeComponents() {

        setSize(new java.awt.Dimension(1200, 800));

        tracePanel = new TracePanel(this);
        tracePanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 6, 12, 12)));

        traceTableModel = new TraceTableModel();
        traceTable = new JTable(traceTableModel){

            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                return traceTableModel.getToolTipText(rowAtPoint(p), columnAtPoint(p));
            }
        };
        TableRenderer renderer = new TableRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4));
        traceTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        traceTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        traceTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        traceTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        traceTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
        traceTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        traceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(!evt.getValueIsAdjusting())
                    traceTableSelectionChanged();
            }
        });

        scrollPane1 = new JScrollPane(traceTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ActionPanel actionPanel1 = new ActionPanel(false);
        actionPanel1.setAddAction(getImportAction());
        actionPanel1.setRemoveAction(getRemoveTraceAction());
        getRemoveTraceAction().setEnabled(false);

        Toolbar controlPanel1 = new Toolbar();
        JButton reloadButton = new JButton(getReloadAction());
        reloadButton.setText("Reload");
        PanelUtils.setupComponent(reloadButton);
        reloadButton.setToolTipText("Reload the selected log file(s)");
        getReloadAction().setEnabled(false);

        controlPanel1.add(actionPanel1);
        controlPanel1.addFlexibleSpace();
        controlPanel1.add(reloadButton);
        controlPanel1.setFloatable(false);
        controlPanel1.setBorder(BorderFactory.createEmptyBorder());

        topPanel = new JPanel(new BorderLayout(0, 0));
        topPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(0, 0, 6, 0)));
        topPanel.add(new JLabel("Trace Files:"), BorderLayout.NORTH);
        topPanel.add(scrollPane1, BorderLayout.CENTER);
        topPanel.add(controlPanel1, BorderLayout.SOUTH);

        statisticTableModel = new StatisticTableModel();
        statisticTable = new JTable(statisticTableModel) {
            //Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };
        statisticTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        statisticTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        statisticTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        statisticTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        statisticTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        statisticTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
//        ComboBoxRenderer comboBoxRenderer = new ComboBoxRenderer(TraceFactory.TraceType.values());
//        comboBoxRenderer.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        statisticTable.getColumnModel().getColumn(3).setPreferredWidth(20);
        statisticTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
        statisticTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        statisticTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(!evt.getValueIsAdjusting())
                    statisticTableSelectionChanged();
            }
        });

        TableEditorStopper.ensureEditingStopWhenTableLosesFocus(statisticTable);

        scrollPane2 = new JScrollPane(statisticTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
//        bottomPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(6, 0, 0, 0)));
        bottomPanel.add(new JLabel("Traces:"), BorderLayout.NORTH);
        bottomPanel.add(scrollPane2, BorderLayout.CENTER);
        JPanel changeTraceTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        changeTraceTypePanel.add(new JLabel("Type:"));

        realButton = new JButton("(R)eal");
        PanelUtils.setupComponent(realButton);
        realButton.setToolTipText(TraceType.REAL.toString());
        realButton.setFont(UIManager.getFont("SmallSystemFont"));
        realButton.setEnabled(false);

        integerButton = new JButton("(I)nt");
        PanelUtils.setupComponent(integerButton);
        integerButton.setToolTipText(TraceType.INTEGER.toString());
        integerButton.setFont(UIManager.getFont("SmallSystemFont"));
        integerButton.setEnabled(false);

        categoricalButton = new JButton("(C)at");
        PanelUtils.setupComponent(categoricalButton);
        categoricalButton.setToolTipText(TraceType.CATEGORICAL.toString());
        categoricalButton.setFont(UIManager.getFont("SmallSystemFont"));
        categoricalButton.setEnabled(false);

        temporalButton = new JButton("(T)ime");
        PanelUtils.setupComponent(temporalButton);
        temporalButton.setToolTipText(TraceType.TEMPORAL.toString());
        temporalButton.setFont(UIManager.getFont("SmallSystemFont"));
        temporalButton.setEnabled(false);

        JLabel constantLabel = new JLabel("* constant");
        PanelUtils.setupComponent(constantLabel);
        constantLabel.setFont(UIManager.getFont("SmallSystemFont"));

        realButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTraceType(TraceType.REAL);
            }
        });
        integerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTraceType(TraceType.INTEGER);
            }
        });
        categoricalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTraceType(TraceType.CATEGORICAL);
            }
        });
        temporalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTraceType(TraceType.TEMPORAL);
            }
        });

        changeTraceTypePanel.add(realButton);
        changeTraceTypePanel.add(integerButton);
        changeTraceTypePanel.add(categoricalButton);
        changeTraceTypePanel.add(temporalButton);
        changeTraceTypePanel.add(constantLabel);
        changeTraceTypePanel.setToolTipText("<html> Change the data type of a selected parameter here. <br>" +
                "Alternatively use key word real, ordinal, binary, categorical " +
                "followed by tab delimited column names <br> in the beginning of the log file, " +
                "to define the trace type. For example: <br> # integer columnName1 columnName2 ... </html>");
        bottomPanel.add(changeTraceTypePanel, BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setPreferredSize(new Dimension(400, 300));
        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
        splitPane1.setBorder(null);

        JPanel progressPanel = new JPanel(new BorderLayout(0, 0));
        progressLabel = new JLabel("");
        progressBar = new JProgressBar();
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(6, 0, 0, 0)));

        leftPanel.add(splitPane1, BorderLayout.CENTER);
        leftPanel.add(progressPanel, BorderLayout.SOUTH);
        leftPanel.setBorder(new BorderUIResource.EmptyBorderUIResource(new java.awt.Insets(12, 12, 12, 6)));

        JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, tracePanel);
        splitPane2.setBorder(null);
        splitPane2.setDividerLocation(350);

        Color focusColor = UIManager.getColor("Focus.color");
        Border focusBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, focusColor);
        splitPane1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        new FileDrop(null, splitPane1, focusBorder, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                importFiles(files);
            }   // end filesDropped
        }); // end FileDrop.Listener

        getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        getContentPane().add(splitPane2, BorderLayout.CENTER);

        splitPane1.setDividerLocation(2000);
    }

    private void changeTraceType(TraceType newType) {
        int[] selectedTraceFiles = traceTable.getSelectedRows();
        int[] selectedStatistics = statisticTable.getSelectedRows();

        boolean combinedTracesSelected = selectedTraceFiles[selectedTraceFiles.length - 1] == traceLists.size();

        List<LogFileTraces> selectedTraceLists = new ArrayList<LogFileTraces>();

        if (combinedTracesSelected) {
            selectedTraceLists.addAll(traceLists);
        } else {
            for (int row : selectedTraceFiles) {
                selectedTraceLists.add(traceLists.get(row));
            }
        }

        for (LogFileTraces tl : selectedTraceLists) {
            for (int row : selectedStatistics) {
                int id = tl.getTraceIndex(commonTraceNames.get(row));

                try {
                    tl.changeTraceType(id, newType);
                } catch (TraceException e) {
                    JOptionPane.showMessageDialog(this, e,
                            "Trace Type Exception in " + tl.getName(), JOptionPane.ERROR_MESSAGE);
                }
                tl.analyseTrace(id);
            }
        }

        updateCombinedTraces();
        statisticTableModel.fireTableDataChanged();
        traceTableModel.fireTableDataChanged();

        // selection will be lost by fireTableDataChanged so reselect them
        statisticTable.getSelectionModel().clearSelection();
        for (int row : selectedStatistics) {
            statisticTable.getSelectionModel().addSelectionInterval(row, row);
        }

        traceTable.getSelectionModel().clearSelection();
        for (int row : selectedTraceFiles) {
            traceTable.getSelectionModel().addSelectionInterval(row, row);
        }

        //traceTableSelectionChanged();
        //statisticTableSelectionChanged();

    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        setupDividerLocation();
    }

    private void setupDividerLocation() {

        if (dividerLocation == -1 || dividerLocation == splitPane1.getDividerLocation()) {
            int h0 = topPanel.getHeight();
            int h1 = scrollPane1.getViewport().getHeight();
            int h2 = traceTable.getPreferredSize().height;
            dividerLocation = h2 + h0 - h1;

//		   	int h0 = topPanel.getHeight() - scrollPane1.getViewport().getHeight();
// 			dividerLocation = traceTable.getPreferredSize().height + h0;

            if (dividerLocation > 400) dividerLocation = 400;
            splitPane1.setDividerLocation(dividerLocation);
        }
    }

    public void setAnalysesEnabled(boolean enabled) {
        getDemographicAction().setEnabled(enabled);
        getBayesianSkylineAction().setEnabled(enabled);
        getGMRFSkyrideAction().setEnabled(enabled);
        getSkyGridAction().setEnabled(enabled);
        getLineagesThroughTimeAction().setEnabled(enabled);
        getCreateTemporalAnalysisAction().setEnabled(enabled);
        getAddDemographicAction().setEnabled(enabled && temporalAnalysisFrame != null);
        getAddBayesianSkylineAction().setEnabled(enabled && temporalAnalysisFrame != null);
        getAddTimeDensityAction().setEnabled(enabled && temporalAnalysisFrame != null);
        getConditionalPosteriorDistAction().setEnabled(enabled);

        getExportAction().setEnabled(enabled);
        getExportRawDataAction().setEnabled(enabled);
        getExportDataTableAction().setEnabled(enabled);
//        getExportFullStatisticsAction().setEnabled(enabled);
        getExportPDFAction().setEnabled(enabled);
        getCopyAction().setEnabled(true);
    }

    public void checkForUniqueNames(LogFileTraces traceList) {

        Map<String, Trace> nameMap = new HashMap<String, Trace>();
        Map<String, Integer> nameNumberMap = new HashMap<String, Integer>();

        for (int i = 0; i < traceList.getTraceCount(); i++) {
            Trace trace = traceList.getTrace(i);
            String name = trace.getName();

            Trace lastTrace = nameMap.get(name);

            if (lastTrace == null) {
                nameMap.put(name, trace);
                nameNumberMap.put(name, 1);
            } else {
                int number = nameNumberMap.get(name);
                if (number == 1) {
                    lastTrace.setName(name + "_" + number);
                }
                number += 1;
                trace.setName(name + "_" + number);
                nameNumberMap.put(name, number);
            }
        }
    }

    public void addTraceList(LogFileTraces traceList) {

        int[] selRows = traceTable.getSelectedRows();

        traceLists.add(traceList);

        updateCombinedTraces();

        setAnalysesEnabled(true);

        traceTableModel.fireTableDataChanged();

        int newRow = traceLists.size() - 1;
        traceTable.getSelectionModel().setSelectionInterval(newRow, newRow);
        if (selRows.length > 1) {
            for (int row : selRows) {
                if (row == traceLists.size() - 1) {
                    row = traceLists.size();
                }
                traceTable.getSelectionModel().addSelectionInterval(row, row);
            }
        }

        setupDividerLocation();

//        allTraceLists.add(traceList);

    }

    private LogFileTraces[] removeTraceList() {
        int[] selRows = traceTable.getSelectedRows();

        LogFileTraces[] tls = new LogFileTraces[selRows.length];
        int i = 0;
        for (int row : selRows) {
            tls[i] = traceLists.get(row);
            i++;
        }
        for (LogFileTraces tl : tls) {
            traceLists.remove(tl);
//            allTraceLists.remove(tl);
        }

        updateCombinedTraces();

        traceTableModel.fireTableDataChanged();
        statisticTableModel.fireTableDataChanged();

        if (traceLists.size() == 0) {
            getRemoveTraceAction().setEnabled(false);

            setAnalysesEnabled(false);

            currentTraceLists.clear();
//            allTraceLists.clear();
            statisticTableModel.fireTableDataChanged();

            tracePanel.setTraces(null, null);
        }

        tracePanel.traceRemoved();

        if (traceLists.size() > 0) {
            int row = selRows[0];
            if (row >= traceLists.size()) {
                row = traceLists.size() - 1;
            }
            traceTable.getSelectionModel().addSelectionInterval(row, row);
        }
        setupDividerLocation();

        return tls;
    }

    // reload all logs
    private void refreshTraceList() {

        final int[] statsSelRows = statisticTable.getSelectedRows();

        Rectangle rect = statisticTable.getVisibleRect();
        int firstRow = statisticTable.rowAtPoint(rect.getLocation());
        double numberOfRows = rect.getHeight()/statisticTable.getRowHeight();
        int lastRow = firstRow + (int)Math.floor(numberOfRows);

        statisticTableModel.setFirstVisibleRow(firstRow);
        statisticTableModel.setLastVisibleRow(lastRow);

        LogFileTraces[] tls = removeTraceList();
        if (tls.length > 0) {
            final LogFileTraces[] newTls = new LogFileTraces[tls.length];

            for (int i = 0; i < tls.length; i++) {
                newTls[i] = new LogFileTraces(tls[i].getName(), tls[i].getFile());
            }

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    processTraces(newTls);
                    return null;
                }

                @Override
                protected void done() {
                    updateCombinedTraces();
                    statisticTableModel.fireTableDataChanged();
                    statisticTable.getSelectionModel().clearSelection();
                    for (int row : statsSelRows) {
                        statisticTable.getSelectionModel().addSelectionInterval(row, row);
                    }
                    traceTableSelectionChanged();
                    statisticTableSelectionChanged();
                }
            };
            worker.execute();

//            updateCombinedTraces();
//
//            statisticTableModel.fireTableDataChanged();
//            statisticTable.getSelectionModel().clearSelection();
//            for (int row : statsSelRows) {
//                statisticTable.getSelectionModel().addSelectionInterval(row, row);
//            }

//            traceTableSelectionChanged();
//            statisticTableSelectionChanged();
        }

    }

    public void setBurnIn(int index, long burnIn) {
        LogFileTraces trace = traceLists.get(index);
        trace.setBurnIn(burnIn);
        analyseTraceList(trace);
        updateCombinedTraces();
        updateTraceTables();
    }

    public void updateCombinedTraces() {
        if (traceLists.size() > 1) {
            LogFileTraces[] traces = new LogFileTraces[traceLists.size()];
            try {
                traceLists.toArray(traces);
            } catch (ArrayStoreException ase) {
                combinedTraces = null;
//                JOptionPane.showMessageDialog(this, "",
//                        "Trace Type Exception",
//                        JOptionPane.WARNING_MESSAGE);
            }
            try {
                combinedTraces = new CombinedTraces("Combined", traces);

                analyseTraceList(combinedTraces);
            } catch (TraceException te) {
                combinedTraces = null; // validations in CombinedTraces()
            }
        } else {
            combinedTraces = null;
        }
    }

    public void updateTraceTables() {
        int[] selectedTraces = traceTable.getSelectedRows();
        int[] selectedStatistics = statisticTable.getSelectedRows();

        traceTableModel.fireTableDataChanged();
        statisticTableModel.fireTableDataChanged();

        traceTable.getSelectionModel().clearSelection();
        for (int row : selectedTraces) {
            traceTable.getSelectionModel().addSelectionInterval(row, row);
        }

        statisticTable.getSelectionModel().clearSelection();
        for (int row : selectedStatistics) {
            statisticTable.getSelectionModel().addSelectionInterval(row, row);
        }
    }

    public void traceTableSelectionChanged() {

        if (traceLists.size() == 0) {
            return;
        }

        int[] selRows = traceTable.getSelectedRows();

        if (selRows.length == 0) {
            getRemoveTraceAction().setEnabled(false);
            getReloadAction().setEnabled(false);
            setAnalysesEnabled(false);
            return;
        }

        setAnalysesEnabled(true);

        getRemoveTraceAction().setEnabled(true);
        getReloadAction().setEnabled(true);

        currentTraceLists.clear();

        // Check for and add the combined trace first so it is underneath the
        // individual traces.
        for (int row : selRows) {
            if (row == traceLists.size()) {
                // Combined is include in the selection so disable remove
                getRemoveTraceAction().setEnabled(false);
                getReloadAction().setEnabled(false);
                currentTraceLists.add(combinedTraces);
            }
        }

        // Get the common set of trace names. This is slightly more complicated
        // that it may seem because we want to keep them in order of the first
        // selected trace file (i.e., as a list). So we populate the list with the
        // first trace file, collect the common set, and then retain only those in
        // the set.
        commonTraceNames.clear();
        homogenousTraceFiles = true;
        Set<String> commonSet = new HashSet<String>();
        boolean isFirst = true;
        for (int row : selRows) {
            if (row < traceLists.size()) {
                TraceList tl = traceLists.get(row);
                Set<String> nameSet = new HashSet<String>();
                for (int i = 0; i < tl.getTraceCount(); i++) {
                    String traceName = tl.getTraceName(i);
                    nameSet.add(traceName);
                    if (isFirst) {
                        // add them in order of the first trace file
                        commonTraceNames.add(traceName);
                    }
                }

                if (isFirst) {
                    commonSet.addAll(nameSet);
                    isFirst = false;
                } else {
                    if (nameSet.size() != commonSet.size()) {
                        homogenousTraceFiles = false;
                    }
                    commonSet.retainAll(nameSet);
                }

                currentTraceLists.add(tl);
            } else if (isFirst) {
                // if the 'Combined' trace is selected but no other trace files, then add all traces
                TraceList tl = traceLists.get(0);
                Set<String> nameSet = new HashSet<String>();
                for (int i = 0; i < tl.getTraceCount(); i++) {
                    String traceName = tl.getTraceName(i);
                    nameSet.add(traceName);
                    commonTraceNames.add(traceName);
                }
                commonSet.addAll(nameSet);
            }
        }
        commonTraceNames.retainAll(commonSet);

        int[] rows = statisticTable.getSelectedRows();

        statisticTableModel.fireTableDataChanged();

        if (rows.length > 0) {
            for (int row : rows) {
                statisticTable.getSelectionModel().addSelectionInterval(row, row);
            }
            statisticTable.scrollRectToVisible(statisticTable.getCellRect(statisticTableModel.getLastVisibleRow(), 0, true));
        } else {
            statisticTable.getSelectionModel().setSelectionInterval(0, 0);
        }

        filterStatus.setText(message);
    }

//    private String updateStatusMessage(List<TraceList> currentTraceLists) {
//        String message = "";
//        List<String> traceNameList = new ArrayList<String>();
//        List<String> messageList = new ArrayList<String>();
//
////        for (int i = 0; i < currentTraceLists.size(); i++) {
////            FilteredTraceList fTL = (FilteredTraceList) currentTraceLists.get(i);
////            Filter f = ).getTraceName();
////
////            if (f != null) {
////                String tN = f.getTraceName();
////                if (!traceNameList.contains(tN)) {
////                    traceNameList.add(tN);
////                    message = f.getStatusMessage() + " in file(s) " + "\'" + fTL.getName() + "\'";
////                    messageList.add(message);
////                } else {
////                    int id = traceNameList.indexOf(tN);
////                    message = messageList.get(id) + " and \'" + fTL.getName() + "\'";
////                    messageList.set(id, message);
////                }
////
////                filterCombo.setSelectedItem(tN);  // todo
////            }
////        }
//
//
//        message = "";
//
//        for (String s : messageList) {
//            message += s + "; ";
//        }
//
//        return message;
//    }

//    private void getIntersectionOfSelectedTraceLists() {
//        filterCombo.removeAllItems();
//        filterCombo.addItem("None");
//
////        Map<String, Class> tracesIntersection = new HashMap<String, Class>(); //names have no order
//        List<String> tracesIntersection = Collections.synchronizedList(new ArrayList<String>());
//        List<TraceFactory.TraceType> tracesIntersectionClass = Collections.synchronizedList(new ArrayList<TraceFactory.TraceType>());
//        List<String> incompatibleTrace = Collections.synchronizedList(new ArrayList<String>());
//        for (TraceList tl : currentTraceLists) {
//            List<String> currentTrace = new ArrayList<String>();
//            for (int i = 0; i < tl.getTraceCount(); i++) {
//                String traceName = tl.getTraceName(i);
//                currentTrace.add(traceName);
//                if (!incompatibleTrace.contains(traceName)) {
//                    TraceFactory.TraceType traceType = tl.getTrace(i).getTraceType();
//                    if (traceType == null) {
//                        incompatibleTrace.add(traceName);
//                        break;
//                    }
//
//                    if (tracesIntersection.contains(traceName)) {
//                        if (traceType != tracesIntersectionClass.get(tracesIntersection.indexOf(traceName))) {
//                            tracesIntersectionClass.remove(tracesIntersection.indexOf(traceName));
//                            tracesIntersection.remove(traceName);
//                            incompatibleTrace.add(traceName);
//                            break;
//                        }
//
//                    } else if (currentTraceLists.indexOf(tl) == 0) {
//
//                        tracesIntersection.add(traceName);
//                        tracesIntersectionClass.add(traceType);
//                    }
//                }
//            } // end i loop
//
//            for (String traceName : tracesIntersection) {
//                if (!currentTrace.contains(traceName)) {
//                    tracesIntersectionClass.remove(tracesIntersection.indexOf(traceName));
//                    tracesIntersection.remove(traceName);
//                    incompatibleTrace.add(traceName);
//                }
//            }
//
//        }
//
//        assert (tracesIntersection.size() == tracesIntersectionClass.size());
//
//        if (!tracesIntersection.isEmpty()) {
//            for (String traceName : tracesIntersection) {
//                filterCombo.addItem(traceName);
//            }
//        }
//    }

    public void statisticTableSelectionChanged() {

        int[] selRows = statisticTable.getSelectedRows();

        boolean isIncomplete = false;
        for (TraceList tl : currentTraceLists) {
            if (tl == null || tl.getTraceCount() == 0 || tl.getStateCount() == 0)
                isIncomplete = true;
        }

        List<String> selectedTraceNames = getSelectedTraces();

        if (currentTraceLists.size() == 0 || isIncomplete) {
            tracePanel.setTraces(null, selectedTraceNames);
        } else {
            TraceList[] tl = new TraceList[currentTraceLists.size()];
            currentTraceLists.toArray(tl);
            try {
                tracePanel.setTraces(tl, selectedTraceNames);
            } catch (ChartRuntimeException cre) {
                JOptionPane.showMessageDialog(this, "One or more traces contain invalid values and \rare not able to be displayed.",
                        "Problem reading trace file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        realButton.setEnabled(selRows.length > 0);
        integerButton.setEnabled(selRows.length > 0);
        categoricalButton.setEnabled(selRows.length > 0);
        temporalButton.setEnabled(selRows.length > 0);
    }

    // get names of selected traces
    public List<String> getSelectedTraces() {

        int[] selRows = statisticTable.getSelectedRows();

        List<String> selectedTraces = new ArrayList<String>();
        for (int selRow : selRows) {
            if (selRow < commonTraceNames.size()) {
                selectedTraces.add(commonTraceNames.get(selRow));
            }
        }
        if (selectedTraces.size() < 1) {
            selectedTraces.add(commonTraceNames.get(0));
        }

        return selectedTraces;
    }

    public void analyseTraceList(TraceList job) {

        if (analyseTask == null) {
            analyseTask = new AnalyseTraceTask();

            javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    progressBar.setMaximum(analyseTask.getLengthOfTask());
                    progressBar.setValue(analyseTask.getCurrent());
                }
            });

            analyseTask.go();
            timer.start();
        }

        analyseTask.add(job);
    }

    AnalyseTraceTask analyseTask = null;

    class AnalyseTraceTask extends LongTask {

        class AnalysisStack<T> {
            private final java.util.List<T> jobs = new ArrayList<T>();

            public synchronized void add(T job) {
                jobs.add(job);
            }

            public synchronized int getCount() {
                return jobs.size();
            }

            public synchronized T get(int index) {
                return jobs.get(index);
            }

            public synchronized void remove(int index) {
                jobs.remove(index);
            }
        }

        private final AnalysisStack<TraceList> analysisStack = new AnalysisStack<TraceList>();

        public AnalyseTraceTask() {
        }

        public void add(TraceList job) {
            analysisStack.add(job);
            current = 0;
        }

        public int getCurrent() {
            return current;
        }

        public int getLengthOfTask() {
            int count = 0;
            for (int i = 0; i < analysisStack.getCount(); i++) {
                count += analysisStack.get(i).getTraceCount();
            }
            return count;
        }

        public void stop() {
        }

        public boolean done() {
            return false;
        }

        public String getDescription() {
            return "Analysing Trace File...";
        }

        public String getMessage() {
            return null;
        }

        public Object doWork() {

            current = 0;
            boolean textCleared = true;

            do {
                if (analysisStack.getCount() > 0) {
                    Object job = analysisStack.get(0);
                    TraceList tl = (TraceList) job;

                    try {
                        for (int i = 0; i < tl.getTraceCount(); i++) {
                            progressLabel.setText("Analysing " + tl.getName() + ":");
                            textCleared = false;
                            tl.analyseTrace(i);
                            repaint();
                            current += 1;
                        }
                    } catch (final Exception ex) {
                        // do nothing. An exception is sometimes fired when burnin is changed whilst in the
                        // middle of an analysis. This doesn't seem to matter as the analysis is restarted.

                        ex.printStackTrace();
//                        EventQueue.invokeLater (
//								new Runnable () {
//									public void run () {
//										JOptionPane.showMessageDialog(TracerFrame.this, "Fatal exception: " + ex.getMessage(),
//												"Error reading file",
//												JOptionPane.ERROR_MESSAGE);
//									}
//								});
                    }
                    analysisStack.remove(0);
                } else {
                    if (!textCleared) {
                        progressLabel.setText("");
                        textCleared = true;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        // do nothing
                    }
                }
            } while (true);
        }

        //private int lengthOfTask = 0;
        private int current = 0;
        //private String message;
    }

    public final void doExportData(boolean rawData) {

        FileDialog dialog = new FileDialog(this,
                (rawData ? "Export Raw Data..." : "Export Data Table..."),
                FileDialog.SAVE);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            try {
                FileWriter writer = new FileWriter(file);
                if (rawData) {
                    try {
                        exportRawData(new PrintWriter(writer));
                    } catch (StateStepsDifferentException ssde) {
                        JOptionPane.showMessageDialog(this, "Unable to write multiple trace files\n" +
                                        "with different step sizes.",
                                "Unable to write file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    writer.write(tracePanel.getExportDataTableText());
                }
                writer.close();


            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to write file: " + ioe,
                        "Unable to write file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class StateStepsDifferentException extends Exception { }

    /**
     * Write out the selected traces to a single file. Checks that when multiple files are
     * selected they have the same step size.
     * @param writer
     */
    private void exportRawData(PrintWriter writer) throws StateStepsDifferentException {
        List<String> selectedTraces = getSelectedTraces();

        long minStateStart = Long.MAX_VALUE;
        long maxStateCount = 0;
        long stateStep = 0;

        for (TraceList tl : currentTraceLists) {
            long stateCount = tl.getStateCount();
            if (stateStep == 0) {
                stateStep = tl.getStepSize();
            } else {
                if (stateStep != tl.getStepSize()) {
                    throw new StateStepsDifferentException();
                }
            }

            if (tl.getBurnIn() < minStateStart) {
                minStateStart = tl.getBurnIn();
            }

            if (stateCount > maxStateCount) {
                maxStateCount = stateCount;
            }
        }

        writer.print("state");
        for (String traceName : selectedTraces) {
            for (TraceList tl : currentTraceLists) {
                String name = tl.getName() + ":" + tl.getTraceName(tl.getTraceIndex(traceName));
                writer.print("\t");
                writer.print(name);
            }
        }
        writer.println();

        for (long state = minStateStart; state <= maxStateCount; state += stateStep) {
            writer.print(state);
            for (String traceName : selectedTraces) {
                for (TraceList tl : currentTraceLists) {
                    int traceIndex = tl.getTraceIndex(traceName);
                    Trace trace = tl.getTrace(traceIndex);

                    writer.print("\t");

                    long burnin = tl.getBurnIn();
                    int index = (int)((state - burnin) / stateStep);

                    if (state >= burnin && index < trace.getValueCount() ) {

                        double value = trace.getValue(index);

                        String valueString;
                        if (trace.getTraceType().isDiscrete()) {
                            if (trace.getTraceType().isCategorical()) {
                                valueString = trace.getCategoryLabelMap().get((int) value);
                            } else {
                                valueString = Integer.toString((int)value);
                            }
                        } else {
                            valueString = Double.toString(value);
                        }
                        writer.print(valueString);
                    } else {
                        // print nothing in this cell
                    }
                }
            }
            writer.println();
        }
    }

    /**
     * export full statistic summary of selected traceList (log) to a tab-delimited txt file
     */
//    public final void doExportStatisticSummary() {
//        final JFrame frame = this;
//
//        FileDialog dialog = new FileDialog(frame, "Export Statistic Summary...", FileDialog.SAVE);
//
//        dialog.setVisible(true);
//        if (dialog.getFile() != null) {
//            File file = new File(dialog.getDirectory(), dialog.getFile());
//
//            // todo use LongTask
//            final String statSummTxt = TraceAnalysis.getStatisticSummary(currentTraceLists);
//
//            try {
//
//                FileWriter writer = new FileWriter(file);
//                writer.write(statSummTxt);
//                writer.close();
//
//            } catch (IOException ioe) {
//                JOptionPane.showMessageDialog(this, "Unable to write file: " + ioe,
//                        "Unable to write file",
//                        JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }

    public final void doExportPDF() {
        FileDialog dialog = new FileDialog(this,
                "Export PDF Image...",
                FileDialog.SAVE);

        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            Rectangle2D bounds = tracePanel.getExportableComponent().getBounds();
            Document document = new Document(new com.lowagie.text.Rectangle((float) bounds.getWidth(), (float) bounds.getHeight()));
            try {
                // step 2
                PdfWriter writer;
                writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                // step 3
                document.open();
                // step 4
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate((float) bounds.getWidth(), (float) bounds.getHeight());
                Graphics2D g2d = tp.createGraphics((float) bounds.getWidth(), (float) bounds.getHeight(), new DefaultFontMapper());
                tracePanel.getExportableComponent().print(g2d);
                g2d.dispose();
                cb.addTemplate(tp, 0, 0);
            } catch (DocumentException de) {
                JOptionPane.showMessageDialog(this, "Error writing PDF file: " + de,
                        "Export PDF Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error writing PDF file: " + e,
                        "Export PDF Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            document.close();
        }
    }


    public final void doImport() {

        if (OSType.isMac()) {
            FileDialog dialog = new FileDialog(this, "Import trace file...", FileDialog.LOAD);
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                importFiles(new File[] {  new File(dialog.getDirectory(), dialog.getFile()) });
            }
        } else {
            final JFileChooser chooser = new JFileChooser(openDefaultDirectory);
            chooser.setMultiSelectionEnabled(true);

            FileNameExtensionFilter filter = new FileNameExtensionFilter("BEAST log (*.log) Files", "log", "txt");
            chooser.setFileFilter(filter);

            final int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                importFiles(files);
            }
        }
    }

    private void importFiles(File[] files) {
        LogFileTraces[] traces = new LogFileTraces[files.length];

        for (int i = 0; i < files.length; i++) {
            traces[i] = new LogFileTraces(files[i].getName(), files[i]);
        }

        processTraces(traces);
    }

    private File openDefaultDirectory = null;

    private void setDefaultDir(File file) {
        final String s = file.getAbsolutePath();
        String p = s.substring(0, s.length() - file.getName().length());
        openDefaultDirectory = new File(p);
        if (!openDefaultDirectory.isDirectory()) {
            openDefaultDirectory = null;
        }
    }

    protected void processTraces(final LogFileTraces[] tracesArray) {

        final JFrame frame = this;

        // set default dir to directory of last file
        setDefaultDir(tracesArray[tracesArray.length - 1].getFile());

        if (tracesArray.length == 1) {
            try {
                final LogFileTraces traces = tracesArray[0];

                final ProgressMonitorInputStream in = new ProgressMonitorInputStream(
                        this,
                        "Reading " + traces.getName(),
                        new FileInputStream(traces.getFile()));
                in.getProgressMonitor().setMillisToDecideToPopup(0);
                in.getProgressMonitor().setMillisToPopup(0);

//                final Reader reader = new InputStreamReader(in);

                Thread readThread = new Thread() {
                    public void run() {
                        try {
                            traces.loadTraces(in);

                            EventQueue.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            analyseTraceList(traces);
                                            checkForUniqueNames(traces);
                                            addTraceList(traces);
                                        }
                                    });

                        } catch (final TraceException te) {
                            EventQueue.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            JOptionPane.showMessageDialog(frame, "Problem with trace file: " + te.getMessage(),
                                                    "Problem with trace file",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                        } catch (final InterruptedIOException iioex) {
                            // The cancel dialog button was pressed - do nothing
                        } catch (final IOException ioex) {
                            EventQueue.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
                                                    "File I/O Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                        }

                    }
                };
                readThread.start();

            } catch (FileNotFoundException fnfe) {
                JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                        "Unable to open file",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fatal exception: " + ex,
                        "Error reading file",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else {
            Thread readThread = new Thread() {
                public void run() {
                    try {
                        for (final LogFileTraces traces : tracesArray) {
//                            final Reader reader = new FileReader(traces.getFile());
                            traces.loadTraces();

                            EventQueue.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            analyseTraceList(traces);
                                            addTraceList(traces);
                                        }
                                    });
                        }

                    } catch (final TraceException te) {
                        EventQueue.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        JOptionPane.showMessageDialog(frame, "Problem with trace file: " + te.getMessage(),
                                                "Problem with trace file",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                    } catch (final InterruptedIOException iioex) {
                        // The cancel dialog button was pressed - do nothing
                    } catch (final IOException ioex) {
                        EventQueue.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        JOptionPane.showMessageDialog(frame, "File I/O Error: " + ioex.getMessage(),
                                                "File I/O Error",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                });
//                    } catch (final Exception ex) {
//                        EventQueue.invokeLater (
//                                new Runnable () {
//                                    public void run () {
//                                        JOptionPane.showMessageDialog(frame, "Fatal exception: " + ex.getMessage(),
//                                                "Error reading file",
//                                                JOptionPane.ERROR_MESSAGE);
//                                    }
//                                });
                    }

                }
            };
            readThread.start();

        }
    }

    protected boolean readFromFile(File file) throws IOException {
        throw new RuntimeException("Cannot read file - use import instead");
    }

    protected boolean writeToFile(File file) {
        throw new RuntimeException("Cannot write file - this is a read-only application");
    }

    public void doCopy() {
        tracePanel.doCopy();
    }

    private TemporalAnalysisFrame temporalAnalysisFrame = null;

    private void doCreateTemporalAnalysis() {
        if (createTemporalAnalysisDialog == null) {
            createTemporalAnalysisDialog = new NewTemporalAnalysisDialog(this);
        }

        if (createTemporalAnalysisDialog.showDialog() == JOptionPane.CANCEL_OPTION) {
            return;
        }

        temporalAnalysisFrame = createTemporalAnalysisDialog.createTemporalAnalysisFrame(this);

        createTemporalAnalysisAction.setEnabled(false);

        addExtendedBayesianSkylineAction.setEnabled(true);
        addBayesianSkylineAction.setEnabled(true);
        addDemographicAction.setEnabled(true);
        addTimeDensity.setEnabled(true);

        temporalAnalysisFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                temporalAnalysisFrame = null;
                createTemporalAnalysisAction.setEnabled(true);
                addBayesianSkylineAction.setEnabled(false);
                addDemographicAction.setEnabled(false);
                addTimeDensity.setEnabled(false);
            }
        });
    }

    public void doDemographic(boolean add) {
        if (demographicDialog == null) {
            demographicDialog = new DemographicDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, or select the Combined trace.",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }


        if (add) {
            if (demographicDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            demographicDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (demographicDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            demographicDialog.createDemographicFrame(currentTraceLists.get(0), this);
        }
    }

    public void doBayesianSkyline(boolean add) {
        if (bayesianSkylineDialog == null) {
            bayesianSkylineDialog = new BayesianSkylineDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (add) {
            if (bayesianSkylineDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            bayesianSkylineDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (bayesianSkylineDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            bayesianSkylineDialog.createBayesianSkylineFrame(currentTraceLists.get(0), this);
        }
    }

    public void doExtendedBayesianSkyline(boolean add) {
        if (extendedBayesianSkylineDialog == null) {
            extendedBayesianSkylineDialog = new ExtendedBayesianSkylineDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (add) {
            if (extendedBayesianSkylineDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            extendedBayesianSkylineDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (extendedBayesianSkylineDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            extendedBayesianSkylineDialog.createExtendedBayesianSkylineFrame(currentTraceLists.get(0), this);
        }
    }

    public void doSkyGrid(boolean add) {
        if (skyGridDialog == null) {
            skyGridDialog = new SkyGridDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (add) {
            if (skyGridDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            skyGridDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (skyGridDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            skyGridDialog.createSkyGridFrame(currentTraceLists.get(0), this);
        }
    }


    public void doGMRFSkyride(boolean add) {
        if (gmrfSkyrideDialog == null) {
            gmrfSkyrideDialog = new GMRFSkyrideDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        if (add) {
            if (gmrfSkyrideDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            gmrfSkyrideDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (gmrfSkyrideDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            gmrfSkyrideDialog.createGMRFSkyrideFrame(currentTraceLists.get(0), this);
        }
    }


    public void doLineagesThroughTime(boolean add) {
        if (lineagesThroughTimeDialog == null) {
            lineagesThroughTimeDialog = new LineagesThroughTimeDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if (add) {
            if (lineagesThroughTimeDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            lineagesThroughTimeDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (lineagesThroughTimeDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            lineagesThroughTimeDialog.createLineagesThroughTimeFrame(currentTraceLists.get(0), this);
        }
    }

    public void doTraitThroughTime(boolean add) {
        if (traitThroughTimeDialog == null) {
            traitThroughTimeDialog = new TraitThroughTimeDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (but not the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if (add) {
            if (traitThroughTimeDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            traitThroughTimeDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
        } else {
            if (traitThroughTimeDialog.showDialog(currentTraceLists.get(0), null) == JOptionPane.CANCEL_OPTION) {
                return;
            }

            traitThroughTimeDialog.createTraitThroughTimeFrame(currentTraceLists.get(0), this);
        }
    }

    private void doAddTimeDensity() {
        if (timeDensityDialog == null) {
            timeDensityDialog = new TimeDensityDialog(this);
        }

        if (currentTraceLists.size() != 1) {
            JOptionPane.showMessageDialog(this, "Please select exactly one trace to do\n" +
                            "this analysis on, (or the Combined trace).",
                    "Unable to perform analysis",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if (timeDensityDialog.showDialog(currentTraceLists.get(0), temporalAnalysisFrame) == JOptionPane.CANCEL_OPTION) {
            return;
        }

        timeDensityDialog.addToTemporalAnalysis(currentTraceLists.get(0), temporalAnalysisFrame);
    }

    private void doFindConditionalPosteriorDistributions() {
//        if (filterDialog == null) {
//        FilterDialog filterDialog = new FilterDialog(this);
//        }

        if (currentTraceLists == null) {
            JOptionPane.showMessageDialog(this, "There is no file being selected !",
                    "Invalid Action",
                    JOptionPane.ERROR_MESSAGE);
        } else if (currentTraceLists.size() > 1) {
            JOptionPane.showMessageDialog(this, "Only one file can be selected each time !",
                    "Invalid Action",
                    JOptionPane.ERROR_MESSAGE);
        } else if (!(currentTraceLists.get(0) instanceof FilteredTraceList)) {
            JOptionPane.showMessageDialog(this, "Filter cannot be applied to Combined Trace List yet !",
                    "Invalid Action",
                    JOptionPane.ERROR_MESSAGE);

        } else {
            FilterListPanel filterListPanel = null;
            if (combinedTraces == null) {
                filterListPanel = new FilterListPanel((FilteredTraceList) currentTraceLists.get(0));
            } else {
                int n = JOptionPane.showConfirmDialog(this,
                        "Because Combined Traces exits, you have to apply filter\n" +
                                "to all files including Combined Traces.\n" +
                                "Would you like to continue or not ?",
                        "Combined Traces Detected", JOptionPane.YES_NO_OPTION);

                if (n == JOptionPane.YES_OPTION) {
                    filterListPanel = new FilterListPanel(combinedTraces);
                }
            }

            if (filterListPanel != null) {
                try {
                    FilterDialog filterDialog = new FilterDialog(this);
                    message = "  " + filterDialog.showDialog(filterListPanel, filterStatus.getText());
                    filterStatus.setText(message);
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(this, "Error : " + e.getMessage(),
                            "Filter Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public JComponent getExportableComponent() {
        return tracePanel.getExportableComponent();
    }

    class TraceTableModel extends AbstractTableModel {
        final String[] columnNames = {"Trace File", "States", "Burn-In"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            int n = traceLists.size();
            if (n == 0 || combinedTraces != null) n++;
            return n;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            TraceList traceList;

            if (traceLists.size() == 0) {
                switch (col) {
                    case 0:
                        return "No files loaded";
                    case 1:
                        return "";
                    case 2:
                        return "";
                }
            } else if (row == traceLists.size()) {
                traceList = combinedTraces;
                switch (col) {
                    case 0:
                        return traceList.getName();
                    case 1:
                        return traceList.getMaxState();
                    case 2:
                        return "-";
                }
            } else {
                traceList = traceLists.get(row);
                switch (col) {
                    case 0:
                        return traceList.getName();
                    case 1:
                        return traceList.getMaxState();
                    case 2:
                        return traceList.getBurnIn();
                }
            }

            return null;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 2) {
                setBurnIn(row, (Long) value);
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return col == 2 && row < traceLists.size();
        }

        public String getToolTipText(int row, int col) {
            if (traceLists.size() == 0) {
                return null;
            } else if (row == traceLists.size()) {
                return "Combined trace files";
            } else {
                return traceLists.get(row).getFullName();
            }

        }
    }

    class StatisticTableModel extends AbstractTableModel {
        final String[] columnNames = {"Statistic", "Mean", "ESS", "Type"};

        private final DecimalFormat formatter = new DecimalFormat("0.###E0");
        private final DecimalFormat formatter2 = new DecimalFormat("####0.###");

        private int firstVisibleRow = 0;
        private int lastVisibleRow = 0;

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (currentTraceLists.size() == 0 || currentTraceLists.get(0) == null) return 0;
            return commonTraceNames.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            String traceName = commonTraceNames.get(row);

            if (col == 0) {
                return traceName;
            }

            if (!homogenousTraceFiles) {
                return "n/a";
            }

            TraceCorrelation td = currentTraceLists.get(0).getCorrelationStatistics(row);
            if (td == null) return "-";
            if (col == 3) {
                if (td.isConstant()) {
                    return "*";
                }
                return td.getTraceType().getBrief();
            }

            double value = 0.0;
            boolean warning = false;
            boolean extremeWarning = false;
            switch (col) {
                case 1:
                    if (td.getTraceType().isCategorical()) return "n/a";
                    value = td.getMean();
                    break;
                case 2:
//                    if (!td.minEqualToMax()) return "-";
                    value = td.getESS();
                    if (Double.isNaN(value) || value < 1 || td.isConstant()) {
                        // assume not applicable; should be tested in the computation
                        return "-";
                    }
                    if (value < 200.0) warning = true;
                    if (value < 100.0) extremeWarning = true;
                    value = Math.round(value);
                    break;
            }

            String string;
            if (Math.abs(value) < 0.1 || Math.abs(value) >= 100000.0) {
                string = formatter.format(value);
            } else string = formatter2.format(value);

            if (warning) {
                return "<html><font color=\"" + (extremeWarning ? "#EE0000" : "#EEAA00") + "\">" + string + "</font></html> ";
            }

            return string;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && currentTraceLists.size() == 1) {
                //TODO store parameter names in a single object
                //parameter names are stored both in currentTraceLists and in commonTraceNames
                //this makes for difficult to maintain code (code below fixes issue #101)
                currentTraceLists.get(0).getTrace(rowIndex).setName(aValue.toString());
                statisticTableModel.fireTableDataChanged();
                commonTraceNames.remove(rowIndex);
                commonTraceNames.add(rowIndex, aValue.toString());
                //suggest to select the row of which the name has been changed
                statisticTable.setRowSelectionInterval(rowIndex, rowIndex);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0 && currentTraceLists.size() == 1;
        }

        public Class getColumnClass(int c) {
            if (getRowCount() == 0) {
                return Object.class;
            }
            return getValueAt(0, c).getClass();
        }

        public void setFirstVisibleRow(int row) {
            firstVisibleRow = row;
        }

        public void setLastVisibleRow(int row) {
            lastVisibleRow = row;
        }

        public int getFirstVisibleRow() {
            return firstVisibleRow;
        }

        public int getLastVisibleRow() {
            return lastVisibleRow;
        }
    }

    public Action getReloadAction() {
        return reloadAction;
    }

//    public Action getExportFullStatisticsAction() {
//        return exportFullStatisticsAction;
//    }

    public Action getExportRawDataAction() {
        return exportRawDataAction;
    }

    public Action getExportDataTableAction() {
        return exportDataTableAction;
    }

    public Action getExportPDFAction() {
        return exportPDFAction;
    }

    public Action getRemoveTraceAction() {
        return removeTraceAction;
    }

    public Action getDemographicAction() {
        return demographicAction;
    }

    public Action getBayesianSkylineAction() {
        return bayesianSkylineAction;
    }

    public Action getExtendedBayesianSkylineAction() {
        return extendedBayesianSkylineAction;
    }

    public Action getSkyGridAction() {
        return skyGridAction;
    }

    public Action getGMRFSkyrideAction() {
        return gmrfSkyrideAction;
    }

    public Action getLineagesThroughTimeAction() {
        return lineagesThroughTimeAction;
    }

    public Action getTraitThroughTimeAction() {
        return traitThroughTimeAction;
    }

    public Action getCreateTemporalAnalysisAction() {
        return createTemporalAnalysisAction;
    }

    public Action getAddDemographicAction() {
        return addDemographicAction;
    }

    public Action getAddBayesianSkylineAction() {
        return addBayesianSkylineAction;
    }

    public Action getAddExtendedBayesianSkylineAction() {
        return addExtendedBayesianSkylineAction;
    }

    public Action getAddTimeDensityAction() {
        return addTimeDensity;
    }

    public Action getConditionalPosteriorDistAction() {
        return getConditionalPostDistAction;
    }

    private final AbstractAction demographicAction = new AbstractAction(AnalysisMenuFactory.DEMOGRAPHIC_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doDemographic(false);
        }
    };

    private final AbstractAction bayesianSkylineAction = new AbstractAction(AnalysisMenuFactory.BAYESIAN_SKYLINE_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doBayesianSkyline(false);
        }
    };

    private final AbstractAction extendedBayesianSkylineAction = new AbstractAction(AnalysisMenuFactory.EXTENDED_BAYESIAN_SKYLINE_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doExtendedBayesianSkyline(false);
        }
    };

    private final AbstractAction skyGridAction = new AbstractAction(AnalysisMenuFactory.SKY_GRID_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doSkyGrid(false);
        }
    };

    private final AbstractAction gmrfSkyrideAction = new AbstractAction(AnalysisMenuFactory.GMRF_SKYRIDE_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doGMRFSkyride(false);
        }
    };

    private final AbstractAction lineagesThroughTimeAction = new AbstractAction(AnalysisMenuFactory.LINEAGES_THROUGH_TIME) {
        public void actionPerformed(ActionEvent ae) {
            doLineagesThroughTime(false);
        }
    };

    private final AbstractAction traitThroughTimeAction = new AbstractAction(AnalysisMenuFactory.TRAIT_THROUGH_TIME) {
        public void actionPerformed(ActionEvent ae) {
            doTraitThroughTime(false);
        }
    };

    private final AbstractAction createTemporalAnalysisAction = new AbstractAction(AnalysisMenuFactory.CREATE_TEMPORAL_ANALYSIS) {
        public void actionPerformed(ActionEvent ae) {
            doCreateTemporalAnalysis();
        }
    };

    private final AbstractAction addDemographicAction = new AbstractAction(AnalysisMenuFactory.ADD_DEMOGRAPHIC_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doDemographic(true);
        }
    };

    private final AbstractAction addBayesianSkylineAction = new AbstractAction(AnalysisMenuFactory.ADD_BAYESIAN_SKYLINE_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doBayesianSkyline(true);
        }
    };

    private final AbstractAction addExtendedBayesianSkylineAction = new AbstractAction(AnalysisMenuFactory.ADD_EXTENDED_BAYESIAN_SKYLINE_RECONSTRUCTION) {
        public void actionPerformed(ActionEvent ae) {
            doExtendedBayesianSkyline(true);
        }
    };

    private final AbstractAction addTimeDensity = new AbstractAction(AnalysisMenuFactory.ADD_TIME_DENSITY) {
        public void actionPerformed(ActionEvent ae) {
            doAddTimeDensity();
        }
    };

    private final AbstractAction getConditionalPostDistAction = new AbstractAction(AnalysisMenuFactory.CONDITIONAL_POST_DIST) {
        public void actionPerformed(ActionEvent ae) {
            doFindConditionalPosteriorDistributions();
        }
    };

    private final AbstractAction removeTraceAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ae) {
            removeTraceList();
        }
    };

    private final AbstractAction reloadAction = new AbstractAction("Reload Trace File(s)...") {
        public void actionPerformed(ActionEvent ae) {
            refreshTraceList();
        }
    };

//    private final AbstractAction exportFullStatisticsAction = new AbstractAction("Export Statistic Summary...") {
//        public void actionPerformed(ActionEvent ae) {
//            doExportStatisticSummary();
//        }
//    };

    private final AbstractAction exportRawDataAction = new AbstractAction("Export Raw Data...") {
        public void actionPerformed(ActionEvent ae) {
            doExportData(true);
        }
    };

    private final AbstractAction exportDataTableAction = new AbstractAction("Export Data Table...") {
        public void actionPerformed(ActionEvent ae) {
            doExportData(false);
        }
    };

    private final AbstractAction exportPDFAction = new AbstractAction("Export PDF...") {
        public void actionPerformed(ActionEvent ae) {
            doExportPDF();
        }
    };

}