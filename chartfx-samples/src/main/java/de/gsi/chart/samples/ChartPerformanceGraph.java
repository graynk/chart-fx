package de.gsi.chart.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.EditAxis;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Small Chart to document performance @25 Hz update rates
 * 
 * @author rstein
 */
public class ChartPerformanceGraph extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartPerformanceGraph.class);
    private static final String FILE1 = "./testdata/ChartPerformanceBenchmark25Hz_V11.1.0_concept_gc_save_minimisation.csv";
    private static final String FILE2 = "./testdata/ChartPerformanceBenchmark25Hz_V11.1.0.csv";

    @Override
    public void start(final Stage primaryStage) {
        final DataSet dataSet1 = loadData(FILE1);
        final DataSet dataSet2 = loadData(FILE2);

        DefaultNumericAxis xAxis = new DefaultNumericAxis("number of samples", "");
        xAxis.setLogAxis(true);
        
        DefaultNumericAxis yAxis = new DefaultNumericAxis("CPU load", "%");
        final XYChart chart = new XYChart(xAxis, yAxis);
        chart.getPlugins().add(new Zoomer()); // standard plugin, useful for most cases
        chart.getPlugins().add(new TableViewer());
        chart.getPlugins().add(new EditAxis());
        chart.getDatasets().addAll(dataSet1, dataSet2); // two data sets

        final Scene scene = new Scene(new StackPane(chart), 800, 600);
        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    private static DataSet loadData(final String fileName) {
        String pathToCsv = ChartPerformanceGraph.class.getResource(fileName).toExternalForm();
        InputStream inputStream = ChartPerformanceGraph.class.getResourceAsStream(fileName);
        DoubleDataSet dataSet = new DoubleDataSet(fileName.replace(".csv", ""));

        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

            int lineCount = 0;
            String row;
            while ((row = csvReader.readLine()) != null) {
                lineCount++;
                // skip first two rows
                if (lineCount <= 2) {
                    continue;
                }

                final String[] data = row.split(",");
                // quick hack, we know that the data has the format of 
                // JavaFx-Chart, ErrorDataSetRenderer, and Reducing LineRenderer each with (n-samples,cpu) tuples
                if (data[2] == null || data[3] == null) {
                    break;
                }

                final double x = Double.parseDouble(data[2]);
                final double y = Double.parseDouble(data[3]);
                dataSet.add(x, y);
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.atError().setCause(e).addArgument(fileName).addArgument(pathToCsv)
                    .log("failed to open '{}' -> '{}'");
        }

        return dataSet;
    }
}
