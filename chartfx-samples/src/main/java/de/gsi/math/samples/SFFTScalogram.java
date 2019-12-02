package de.gsi.math.samples;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.UpdateAxisLabels;
import de.gsi.chart.renderer.spi.ContourDataSetRenderer;
import de.gsi.chart.renderer.spi.utils.ColorGradient;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet3D;
import de.gsi.dataset.spi.DataSetBuilder;
import de.gsi.math.TMath;
import de.gsi.math.samples.utils.AbstractDemoApplication;
import de.gsi.math.samples.utils.DemoChart;
import de.gsi.math.spectra.fft.ShortTermFastFourierTransform;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * example illustrating Short Term Fourier Transform
 * 
 * @author akrimm
 */
public class SFFTScalogram extends AbstractDemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFFTScalogram.class);
    private static final int MAX_POINTS = 2048 * 2;
    public static final boolean LOAD_EXAMPLE_DATA = false;
    final static double SAMPLE_RATE = 1e-6;
    final static int N_FFT = 512;
    final static double SFFT_OVERLAP = 0.9;
    private DataSet rawDataSet;

    private DataSet3D createDataSet() {
        double[] yValues = loadSyntheticData();

        double[] tValues = new double[yValues.length];
        for (int i = 0; i < tValues.length; i++) {
            tValues[i] = i * SAMPLE_RATE;
        }
        rawDataSet = new DataSetBuilder("testData").setXValuesNoCopy(tValues).setYValues(yValues).build();
        rawDataSet.getAxisDescription(DataSet.DIM_X).set("time", "s");
        rawDataSet.getAxisDescription(DataSet.DIM_Y).set("amplitude", "a.u.");

        DataSet3D fdataset = ShortTermFastFourierTransform.getSpectrogram(rawDataSet, N_FFT, SFFT_OVERLAP);

        return fdataset;
    }

    @Override
    public Node getContent() {

        final DemoChart chart1 = new DemoChart();
        chart1.getPlugins().add(new UpdateAxisLabels());
        final ContourDataSetRenderer contourChartRenderer = new ContourDataSetRenderer();
        chart1.getRenderers().set(0, contourChartRenderer);
        chart1.getPlugins().removeIf(plugin -> plugin instanceof DataPointTooltip);
        // chart1.getAxes().addAll(contourChartRenderer.getFirstAxis(Orientation.HORIZONTAL),
        // contourChartRenderer.getFirstAxis(Orientation.VERTICAL), contourChartRenderer.getZAxis());
        contourChartRenderer.setColorGradient(ColorGradient.VIRIDIS);
        DataSet3D data = createDataSet();
        // contourChartRenderer.getDatasets().add(data); // Does not update axis ranges
        chart1.getDatasets().add(data);

        final DemoChart chart2 = new DemoChart();
        // remove datapointTooltip because of bug
        chart2.getPlugins().removeIf(plugin -> plugin instanceof DataPointTooltip);
        chart2.getPlugins().add(new UpdateAxisLabels());
        chart2.getDatasets().addAll(rawDataSet);

        return new VBox(chart1, chart2);
    }

    private static double[] loadSyntheticData() {
        // synthetic data
        final double[] yModel = new double[MAX_POINTS];

        final Random rnd = new Random();
        for (int i = 0; i < yModel.length; i++) {
            final double x = i;
            double offset = 0;
            final double error = 0.1 * rnd.nextGaussian();

            // linear chirp with discontinuity
            offset = (i > 0.9 * MAX_POINTS) ? -20 : 0;
            yModel[i] = (i > 0.2 * MAX_POINTS && i < 0.9 * MAX_POINTS)
                    ? 0.7 * Math.sin(TMath.TwoPi() * 2e-4 * x * (x + offset)) : 0;

            // single tone at 0.25
            yModel[i] += (i > 0.05 * MAX_POINTS && i < 0.95 * MAX_POINTS) ? 1.0 * Math.sin(TMath.TwoPi() * 0.25 * x)
                    : 0;

            // modulation around 0.4
            final double mod = Math.cos(TMath.TwoPi() * 0.01 * x);
            yModel[i] += (i > 0.3 * MAX_POINTS && i < 0.9 * MAX_POINTS)
                    ? 1.0 * Math.sin(TMath.TwoPi() * (0.4 - 5e-4 * mod) * x) : 0;

            // quadratic chirp starting at 0.1
            yModel[i] += 0.5 * Math.sin(TMath.TwoPi() * ((0.1 + 5e-8 * x * x) * x));

            yModel[i] = yModel[i] + error;
        }
        return yModel;
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

}
