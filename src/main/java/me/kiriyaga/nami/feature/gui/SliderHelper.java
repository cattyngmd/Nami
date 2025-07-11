package me.kiriyaga.nami.feature.gui;

public class SliderHelper {

    /**
     * Counts value for slider by minmax
     * @param oldValue current value
     * @param deltaX delta betwen values
     * @param step step allowed change
     * @param min minimum allowed value
     * @param max maximum allowed value
     */

    // TODO value scoped steps
    public static double slideDouble(double oldValue, double deltaX, double step, double min, double max) {
        double sensitivity = 0.05;
        double change = deltaX * step * sensitivity;
        double newValue = oldValue + change;

        newValue = Math.round(newValue / step) * step;

        if (newValue < min) newValue = min;
        if (newValue > max) newValue = max;

        return newValue;
    }

    public static int slideInt(int oldValue, double deltaX, int step, int min, int max) {
        double val = slideDouble(oldValue, deltaX, step, min, max);
        return (int) val;
    }
}
