package it.adsc.chart;

import javax.swing.*;
import java.awt.*;

/**
 * Created by prageeth.g on 16/11/2015.
 */
public class ChartMainFrame extends JFrame {

    private static ChartMainFrame chartMainFrame;
    private LogChartPanel logChartPanel;

    private ChartMainFrame() {
        setupGUI();
    }

    public ChartMainFrame getInstance()
    {
        if(chartMainFrame == null)
        {
            chartMainFrame = new ChartMainFrame();
        }
        return chartMainFrame;
    }

    private void setupGUI()
    {
        logChartPanel = LogChartPanel.getInstance();
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(logChartPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        ChartMainFrame frame = new ChartMainFrame();
        frame.setSize(new Dimension(1400,800));
        frame.setVisible(true);
        LogChartPanel.getInstance().startFileReadThread();
        LogChartPanel.getInstance().startDataThread();
    }
}
