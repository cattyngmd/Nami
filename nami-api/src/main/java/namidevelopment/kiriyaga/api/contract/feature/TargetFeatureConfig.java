package namidevelopment.kiriyaga.api.contract.feature;

public interface TargetFeatureConfig {

    double getTargetRange();
    double getMinTicksExisted();

    boolean targetPlayers();
    boolean targetHostiles();
    boolean targetNeutrals();
    boolean targetPassives();
    boolean targetProjectiles();

    TargetPriority getPriority();

    enum TargetPriority {
        DISTANCE, HEALTH, SMART
    }
}
