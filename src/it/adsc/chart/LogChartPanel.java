package it.adsc.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Created by prageeth.g on 16/11/2015.
 */
public class LogChartPanel extends JPanel {
    public static final String LOG_FILE_PATH = "C:\\Users\\prageeth.g\\git\\mainprojects\\ema\\attestation\\AttestationClient\\TimeLogger.log";
    public static final String CHART_IMAGE_FILE_PATH = ".\\web\\ChartMonitor\\image\\TheFuture.jpg";
    private static LogChartPanel instance;
    private ChartPanel chartPanel;
    private long lastFileChangedTimestamp = 0;
    private JFreeChart xylineChart = ChartFactory.createXYLineChart(
            "Attestation Chart Logger",
            "Date",
            "Checksum Computation Time",
            createDataset(),
            PlotOrientation.VERTICAL,
            true, true, false);
    XYSeries compTimeSeries = new XYSeries("Computation Time");
    XYSeries averageTimeSeries = new XYSeries("Average Time");

    private LogChartPanel() {
        setupGUI();
    }

    public static LogChartPanel getInstance() {
        if (instance == null) {
            instance = new LogChartPanel();
        }
        return instance;
    }

    private void setupGUI() {
        this.setLayout(new BorderLayout());
        chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
      createChart();
        this.add(chartPanel, BorderLayout.CENTER);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    private XYDataset createDataset() {
        if(compTimeSeries == null)
        {
            compTimeSeries = new XYSeries("Computation Time");
        }
        if(averageTimeSeries == null)
        {
            averageTimeSeries = new XYSeries( "Average Time" );
        }
        dataset = new XYSeriesCollection();
        dataset.addSeries(averageTimeSeries);
        dataset.addSeries(compTimeSeries);
        return dataset;
    }

    private void createChart()
    {
        xylineChart = ChartFactory.createXYLineChart(
                "Attestation Logger Chart",
                "Computation Count",
                "Time (ms)",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        final XYPlot plot = xylineChart.getXYPlot();
        plot.getRangeAxis().setRange(740, 800);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//        renderer.setSeriesPaint(0, Color.BLACK);
//        renderer.setSeriesPaint(1, Color.RED);
//        renderer.setSeriesPaint(2, Color.YELLOW);
        renderer.setSeriesStroke(0, new BasicStroke(0.50f));
        renderer.setSeriesStroke(1, new BasicStroke(0.1f));
//      renderer.setSeriesStroke( 2 , new BasicStroke( 2.0f ) );
//      plot.getDomainAxis().setTickMarksVisible(false);
//      plot.getRangeAxis().setTickMarksVisible(false);
//      plot.getDomainAxis().setTickLabelsVisible(false);
//      plot.getRangeAxis().setTickLabelsVisible(false);
//      plot.getDomainAxis().setAutoTickUnitSelection(false);
//      plot.getRangeAxis().setAutoTickUnitSelection(false);
        plot.setRenderer(renderer);
    }


    public void startDataThread() {
        SwingWorker swingWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                if (xylineChart != null) {
                    xylineChart.getXYPlot().setDataset(xylineChart.getXYPlot().getDataset());
                }
                if(chartPanel != null)
                {
                    System.out.println("Repainting...!");
                    LogChartPanel.this.removeAll();
                    LogChartPanel.this.revalidate(); // This removes the old chart
                    createChart();
                    chartPanel = new ChartPanel(xylineChart);
//                        LogChartPanel.this.setLayout(new BorderLayout());
                    LogChartPanel.this.add(chartPanel, BorderLayout.CENTER);
                    LogChartPanel.this.repaint();
                }
                LogChartPanel.this.updateUI();

                ChartUtilities.saveChartAsJPEG(
                        new java.io.File(CHART_IMAGE_FILE_PATH), xylineChart, 1000, 600);
                return null;
            }
        };
        swingWorker.execute();
//        Thread thread = new Thread(new Runnable() {
//            int count = 0;
//
//            @Override
//            public void run() {
//                while (true) {

                }
//            }
//        });
//
//        thread.run();
//    }

    public void startFileReadThread()
    {
        Thread thread = new Thread(new Runnable() {
            int count = 0;

            @Override
            public void run() {
                BufferedReader br = null;
                File file = new File(LOG_FILE_PATH);

                while (true) {
                    try {
                        FileInputStream fis = new FileInputStream(file);

                        //Construct BufferedReader from InputStreamReader
                        br = new BufferedReader(new InputStreamReader(fis));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                   if(br != null)
                   {
                       String line = "";
                       try {
                           int localCount = 0;
                           System.out.println("File Reading...!");

                           while ((line = br.readLine()) != null) {
                               localCount++;
                               if(count < localCount)
                               {
                                   addChartRecord(line, localCount);
                                   count++;
                               }
                           }
                           System.out.println("File Reading DONE.");
                           SwingUtilities.invokeLater(new Runnable() {
                               @Override
                               public void run() {
                                   startDataThread();
                               }
                           });

//                           xylineChart.setNotify(true);
//                           xylineChart.getXYPlot().setDataset(xylineChart.getXYPlot().getDataset());

                       } catch (IOException e) {
                           e.printStackTrace();
                       }

                       try {
                           br.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.run();
    }
    double averageTime = 0;
    double totalTime = 0;
    private void addChartRecord(String line, int count) {
        //[ 2015-11-16 16:46:06.436740 ]- Success : Time Diff = 677.135986
        if(count == 0)
        {
            return;
        }
        int begin = line.indexOf('=');
        line = line.substring(begin+1).trim();
        double value = Double.parseDouble(line);

        totalTime += value;
        averageTime = totalTime/count;
        if(value < 400 || totalTime < 400 || averageTime < 400)
        {
            System.out.println("count = " + count);
        }
        dataset.getSeries(0).add(count, averageTime);
        dataset.getSeries(1).add(count, value);
        if(count >= 1000 && dataset.getSeries(0).getItemCount() > 1100)
        {
//            int removeRecords = dataset.getSeries(0).getItemCount() - 1000;
//            for(int i = 0; i < removeRecords; i++)
//            {
                dataset.getSeries(0).remove(0);
                dataset.getSeries(1).remove(0);
//            }
        }
        xylineChart.getXYPlot().setDataset(dataset);
    }


    private boolean isFileUpdated( File file ) {
        long timeStamp = file.lastModified();

        if( this.lastFileChangedTimestamp != timeStamp ) {
            this.lastFileChangedTimestamp = timeStamp;
            //Yes, file is updated
            return true;
        }
        //No, file is not updated
        return false;
    }

    private void generateChartImage(File file)
    {
        XYSeries timeSeqies = new XYSeries("Computation Time");
        if(compTimeSeries == null)
        {
            compTimeSeries = new XYSeries("Computation Time");
        }
        if(averageTimeSeries == null)
        {
            averageTimeSeries = new XYSeries( "Average Time" );
        }
        dataset = new XYSeriesCollection();
        dataset.addSeries(compTimeSeries);
        dataset.addSeries(averageTimeSeries);
    }
}
