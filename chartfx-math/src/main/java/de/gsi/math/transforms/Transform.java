package de.gsi.math.transforms;

import de.gsi.dataset.DataSet;

/**
 * Interface for transforms on DataSets.
 * All classes providing this interface, allow to update a DataSet based on another one.
 * 
 * @author Alexander Krimm
 */
public interface Transform {
    /**
     * Perform the transform on a DataSet into a new DataSet.
     * The result is returned immediately and a background job is started to update the result Dataset.
     * The progress is written and updated to the result DataSet's metadata and can be queried with the
     * getStatus Method. Additionally the waitForCompletion() Method blocks until the computation is finished.
     * The result DataSet can be updated periodically during the transformation (if implemented) by setting updateStep <
     * 1.0.
     * 
     * @param input Input DataSet
     * @return output Dataset
     */
    public DataSet transform(final DataSet input);

    /**
     * Perform the transform on a DataSet into an existing DataSet.
     * The old contents of the DataSet are overwritten. The output DataSet must be compatible with the result of the 
     * transform in terms of type and size.
     * The method returns immediately and a background job is started to update the output DataSet.
     * The progress is written and updated to the result DataSet's metadata and can be queried with the
     * getStatus Method. Additionally the waitForCompletion() Method blocks until the computation is finished.
     * The result DataSet can be updated periodically during the transformation (if implemented) by setting updateStep <
     * 1.0.
     * 
     * @param input Input DataSet
     * @param output Output DataSet
     */
    public void transform(final DataSet input, final DataSet output);

    /**
     * Same as transform, but the transform installs a listener to the original Dataset and keeps updating the result
     * whenever the input changes.
     * 
     * @param input
     * @return
     */
    public DataSet transformContinuous(final DataSet input);

    /**
     * Same as transform, but the transform installs a listener to the original Dataset and keeps updating the result
     * whenever the input changes.
     * 
     * @param input
     * @param output
     */
    public void transformContinuous(final DataSet input, final DataSet output);

    /**
     * Sets the step size for applying updates to the result as a fraction of 1.0.
     * Default is 1.0, which means the data is only updated after the computation has finished.
     * A value of 0.05 means the data is added in 5% steps.
     * 
     * @param step update step size as a fraction of 1.0
     */
    public void setUpdateSteps(final double step);

    /**
     * @return step update step size as a fraction of 1.0
     */
    public double getUpdateSteps();

}
