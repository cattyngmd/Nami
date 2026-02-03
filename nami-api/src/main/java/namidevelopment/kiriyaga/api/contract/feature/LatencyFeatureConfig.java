package namidevelopment.kiriyaga.api.contract.feature;

public interface LatencyFeatureConfig {

    Mode getMode();

    int getSmoothingStrength();
    int getUnstableTimeout();
    int getKeepAliveInterval();

    enum Mode {
        OLD, NEW, OFF
    }
}
