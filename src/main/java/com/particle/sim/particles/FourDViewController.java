package com.particle.sim.particles;

import com.particle.sim.math.Math4d;
import com.particle.sim.math.RotationPlane4d;

/**
 * Small diagnostic controller for incrementally orienting a four-dimensional view.
 * It deliberately exposes only the three planes involving W; normal XYZ navigation
 * remains the responsibility of the existing camera.
 */
public final class FourDViewController {
    private static final int REORTHONORMALIZE_INTERVAL = 256;
    private FourDViewConfiguration configuration = FourDViewConfiguration.defaults();
    private int rotationsSinceOrthonormalization;

    public FourDViewConfiguration configuration() {
        return configuration;
    }

    public void resetOrientation() {
        rotationsSinceOrthonormalization = 0;
        replace(configuration.visualizationMode(), Math4d.identity());
    }

    public void rotateXw(double angleRadians) {
        rotate(RotationPlane4d.XW, angleRadians);
    }

    public void rotateYw(double angleRadians) {
        rotate(RotationPlane4d.YW, angleRadians);
    }

    public void rotateZw(double angleRadians) {
        rotate(RotationPlane4d.ZW, angleRadians);
    }

    public void visualizationMode(FourDVisualizationMode mode) {
        replace(mode, configuration.rotationMatrix());
    }

    public void perspectiveDistance(double distance) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), distance, configuration.sliceCenterW(),
                configuration.sliceThickness(), configuration.sliceFeather(), configuration.colorRange());
    }

    public void slice(double centerW, double thickness, double feather) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), configuration.perspectiveDistance(), centerW,
                thickness, feather, configuration.colorRange());
    }

    public void colorRange(double range) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), configuration.perspectiveDistance(),
                configuration.sliceCenterW(), configuration.sliceThickness(), configuration.sliceFeather(), range);
    }

    private void rotate(RotationPlane4d plane, double angleRadians) {
        double[] increment = Math4d.planeRotation(plane, angleRadians);
        double[] orientation = Math4d.multiply(increment, configuration.rotationMatrix());
        rotationsSinceOrthonormalization++;
        if (rotationsSinceOrthonormalization >= REORTHONORMALIZE_INTERVAL) {
            orientation = Math4d.orthonormalize(orientation);
            rotationsSinceOrthonormalization = 0;
        }
        replace(configuration.visualizationMode(), orientation);
    }

    private void replace(FourDVisualizationMode mode, double[] rotationMatrix) {
        configuration = new FourDViewConfiguration(mode, rotationMatrix, configuration.perspectiveDistance(),
                configuration.sliceCenterW(), configuration.sliceThickness(), configuration.sliceFeather(),
                configuration.colorRange());
    }
}
