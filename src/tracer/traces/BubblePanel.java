package tracer.traces;


import dr.app.gui.chart.JChartPanel;
import dr.inference.trace.TraceCorrelation;
import dr.inference.trace.TraceList;
import jam.framework.Exportable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Walter Xie
 */
public class BubblePanel extends JPanel implements Exportable {

    private TangHuLuChart tangHuLuChart = new TangHuLuChart();
    private JChartPanel chartPanel = new JChartPanel(tangHuLuChart, null, "", "");

    /**
     * Creates new IntervalsPanel
     */
    public BubblePanel() {
        setOpaque(false);
        setMinimumSize(new Dimension(300, 150));
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
    }


    public void setTraces(TraceList[] traceLists, java.util.List<String> traceNames) {

        tangHuLuChart.removeAllTangHuLus();

        if (traceLists == null || traceNames == null) {
            chartPanel.setXAxisTitle("");
            chartPanel.setYAxisTitle("");
            return;
        }

        for (TraceList traceList : traceLists) {
            for (String traceName : traceNames) {
                int index = traceList.getTraceIndex(traceName);
                TraceCorrelation td = traceList.getCorrelationStatistics(index);
                if (td != null) {
                    String name = "";
                    if (traceLists.length > 1) {
                        name = traceList.getName();
                        if (traceNames.size() > 1) {
                            name += ": ";
                        }
                    }
                    name += traceName;

                    //todo


                    tangHuLuChart.addTangHuLu(name, td.getIndexMap());
                }
            }
        }

        chartPanel.setXAxisTitle("");
        if (traceLists.length == 1) {
            chartPanel.setYAxisTitle(traceLists[0].getName());
        } else if (traceNames.size() == 1) {
            chartPanel.setYAxisTitle(traceNames.get(0));
        } else {
            chartPanel.setYAxisTitle("Multiple Traces");
        }
        add(chartPanel, BorderLayout.CENTER);

        validate();
        repaint();
    }

    @Override
    public JComponent getExportableComponent() {
        return chartPanel;
    }
}
