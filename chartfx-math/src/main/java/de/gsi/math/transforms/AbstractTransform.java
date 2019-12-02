package de.gsi.math.transforms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Krimm
 */
abstract public class AbstractTransform implements Transform{
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransform.class);
     
     private double lastStep = 0.0;
     private double updateStep = 1.0;
     private double progress = 0.0;

    @Override
    public void setUpdateSteps(double step) {
        if (step < 0 | step > 1.0) {
            throw new IllegalArgumentException("UpdateStep value must be between 0.0 and 1.0");
        }
        updateStep = step;
    }

    @Override
    public double getUpdateSteps() {
        return updateStep;
    }
}

