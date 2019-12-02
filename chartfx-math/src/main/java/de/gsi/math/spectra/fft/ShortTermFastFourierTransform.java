package de.gsi.math.spectra.fft;

import static de.gsi.dataset.DataSet.DIM_X;
import static de.gsi.dataset.DataSet.DIM_Y;
import static de.gsi.dataset.DataSet.DIM_Z;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet3D;
import de.gsi.dataset.spi.DoubleDataSet3D;
import de.gsi.math.spectra.SpectrumTools;

/**
 * @author Alexander Krimm
 */
public class ShortTermFastFourierTransform {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortTermFastFourierTransform.class);

    /**
     * Perform a Short term fourier transform
     * 
     * @param input a dataset with equidistantly spaced y(t) data
     * @param nQuantf the number of frequency bins
     * @param overlap the overlap between consecutive ffts
     * @return the spectrogram, a DataSet3D with dimensions [nf = nQuantx x nY = ]
     */
    public static DataSet3D getSpectrogram(final DataSet input, final int nQuantf, final double overlap) {
        int nQuantt = (int) Math.floor((input.getDataCount() - nQuantf) / (nQuantf * (1 - overlap)));
        return getSpectrogram(input, nQuantf, nQuantt);
    }

    /**
     * Perform a Short term fourier transform
     * 
     * @param input a dataset with equidistantly spaced y(t) data
     * @param nQuantf the number of frequency bins
     * @param nQuantt the number of time bins
     * @return the spectrogram, a DataSet3D with dimensions [nf = nQuantx x nY = ]
     */
    public static DataSet3D getSpectrogram(final DataSet input, int nQuantf, int nQuantt) {
        // validate input data
        if (input.getDataCount(DIM_X) < nQuantf) {
            LOGGER.atWarn().addArgument(nQuantf).log("Not enough samples for requested frequency resolution");
            nQuantf = input.getDataCount(DIM_X);
        }
        if (input.getDataCount(DIM_X) - nQuantf < nQuantt - 1) {
            LOGGER.atWarn().addArgument(nQuantt).log("Not enough samples for requested time resolution: {}");
            nQuantt = input.getDataCount(DIM_X) - nQuantf + 1;
        }

        // set time axis
        double[] timeAxis = new double[nQuantt];
        for (int i = 0; i < nQuantt; i++) {
            timeAxis[i] = input.get(DIM_X,
                    Math.floorDiv(i * (input.getDataCount(DIM_X) - nQuantf), nQuantt) + Math.floorDiv(nQuantf, 2));
        }

        // set frequency axis
        double fs = input.getDataCount(DIM_X) / (input.get(DIM_X, input.getDataCount(DIM_X) - 1) - input.get(DIM_X, 0));
        double fStep = fs / nQuantf;
        double[] frequencyAxis = new double[nQuantf / 2];
        for (int i = 0; i < nQuantf / 2; i++) {
            frequencyAxis[i] = i * fStep;
        }

        // set amplitude data
        double[][] amplitudeData = new double[nQuantt][];
        double amplitudeMin = Double.POSITIVE_INFINITY;
        double amplitudeMax = Double.NEGATIVE_INFINITY;

        // calculate scalogram
        final DoubleFFT_1D fastFourierTrafo = new DoubleFFT_1D(nQuantf);
        double[] raw = new double[nQuantf];
        double[] mean = new double[nQuantf / 2];
        double[] current = new double[nQuantf / 2];
        final int timestep = Math.floorDiv(input.getDataCount() - nQuantf, nQuantt);
        for (int i = 0; i < nQuantt; i++) {
            LOGGER.atDebug().addArgument(i * timestep).addArgument(i * timestep + nQuantf)
                    .log("SFFT: evaluating slice from {} to {}");
            for (int j = 0; j < nQuantf; j++) {
                raw[j] = input.get(DIM_Y, i * timestep + j);
            }
            fastFourierTrafo.realForward(raw);
            amplitudeData[i] = SpectrumTools.computeMagnitudeSpectrum_dB(raw, true);
            // do projection to frequency axis?
            current = SpectrumTools.computeMagnitudeSpectrum_dB(raw, true);
            for (int j = 0; j < nQuantf / 2; j++) {
                amplitudeMin = Math.min(amplitudeMin, amplitudeData[i][j]);
                amplitudeMax = Math.max(amplitudeMax, amplitudeData[i][j]);
                mean[j] += current[j];
            }
        }
        for (int j = 0; j < nQuantf / 2; j++) {
            mean[j] /= nQuantt;
        }

        // initialize result dataset
        DoubleDataSet3D result = new DoubleDataSet3D("SFFT(" + input.getName() + ")", frequencyAxis, timeAxis,
                amplitudeData);
        result.getInfoList().add("nFFT=" + nQuantf + ", nT=" + nQuantt);

        // Set Axis Labels and Units
        result.getAxisDescription(DIM_Y).set("Time", input.getAxisDescription(DIM_X).getUnit(), timeAxis[0],
                timeAxis[timeAxis.length - 1]);
        result.getAxisDescription(DIM_X).set("Frequency", "1/" + input.getAxisDescription(DIM_X).getUnit(),
                frequencyAxis[0], frequencyAxis[frequencyAxis.length - 1]);
        result.getAxisDescription(DIM_Z).set("Magnitude", input.getAxisDescription(DIM_Y).getUnit(), amplitudeMin,
                amplitudeMax);
        LOGGER.atInfo().addArgument(result).log("result of sfft: {}");
        return result;
    }
}
