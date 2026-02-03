package namidevelopment.kiriyaga.api.contract.feature;

public interface RotationsFeatureConfig {

    RotationMode getRotationMode();

    double getRotationSpeed();
    double getRotationEase();
    double getRotationThreshold();

    boolean isJitterEnabled();
    boolean isMoveFixEnabled();
    boolean isRenderEnabled();
    int getHoldTicks();

    enum RotationMode {
        MOTION, SILENT
    }
}
