package de.gsi.math.samples;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.UpdateAxisLabels;
import de.gsi.chart.renderer.spi.ContourDataSetRenderer;
import de.gsi.chart.renderer.spi.MetaDataRenderer;
import de.gsi.chart.renderer.spi.utils.ColorGradient;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet3D;
import de.gsi.dataset.DataSetMetaData;
import de.gsi.dataset.spi.DataSetBuilder;
import de.gsi.math.TMath;
import de.gsi.math.samples.utils.AbstractDemoApplication;
import de.gsi.math.samples.utils.DemoChart;
import de.gsi.math.spectra.fft.ShortTermFastFourierTransform;
import de.gsi.math.spectra.wavelet.ContinuousWavelet;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * example illustrating Short Term Fourier Transform
 * 
 * @author akrimm
 */
public class SFFTScalogram extends AbstractDemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFFTScalogram.class);
    private static final int MAX_POINTS = 2048 * 2;
    final static double SAMPLE_RATE = 1e-6;

    // sfft constants
    final static int N_FFT = 256;
    final static double SFFT_OVERLAP = 0.9;

    // wavelet constants
    final static int nQuantx = 512;
    final static int nQuanty = 1024;
    final static double nu = 2 * 25;
    final static double fmin = 0.05;
    final static double fmax = 0.5;

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
        ((DataSetMetaData) rawDataSet).getInfoList().add("test");

        DataSet3D fdataset = ShortTermFastFourierTransform.getSpectrogram(rawDataSet, N_FFT, SFFT_OVERLAP);

        ((DataSetMetaData) fdataset).getInfoList().add("test2");
        return fdataset;
    }

    @Override
    public Node getContent() {

        final DemoChart chart1 = new DemoChart();
        chart1.getPlugins().add(new UpdateAxisLabels());
        final ContourDataSetRenderer contourChartRenderer1 = new ContourDataSetRenderer();
        chart1.getRenderers().set(0, contourChartRenderer1);
        chart1.getRenderers().add(new MetaDataRenderer(chart1));
        chart1.getPlugins().removeIf(plugin -> plugin instanceof DataPointTooltip);
        DefaultNumericAxis xAxis = new DefaultNumericAxis();
        xAxis.setSide(Side.BOTTOM);
        DefaultNumericAxis yAxis = new DefaultNumericAxis();
        yAxis.setSide(Side.LEFT);
        Axis zAxis = contourChartRenderer1.getZAxis();
        chart1.getAxes().addAll(xAxis, yAxis, zAxis);
        contourChartRenderer1.setColorGradient(ColorGradient.VIRIDIS);
        DataSet3D dataSFFT = createDataSet();
        // contourChartRenderer.getDatasets().add(data); // Does not update axis ranges
        chart1.getDatasets().add(dataSFFT);

        final DemoChart chart2 = new DemoChart();
        chart2.getPlugins().add(new UpdateAxisLabels());
        final ContourDataSetRenderer contourChartRenderer2 = new ContourDataSetRenderer();
        chart2.getRenderers().set(0, contourChartRenderer2);
        chart2.getRenderers().add(new MetaDataRenderer(chart2));
        chart2.getPlugins().removeIf(plugin -> plugin instanceof DataPointTooltip);
        contourChartRenderer2.setColorGradient(ColorGradient.VIRIDIS);
        ContinuousWavelet wtrafo = new ContinuousWavelet();
        DataSet3D dataWavelet = wtrafo.getScalogram(rawDataSet.getValues(DataSet.DIM_X), nQuantx, nQuanty, nu, fmin,
                fmax);
        chart2.getDatasets().add(dataWavelet);

        return new HBox(chart1, chart2);
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
