package namidevelopment.kiriyaga.api.contract;

import java.util.HashMap;
import java.util.Map;

public class FeatureContractService {

    private static final Map<Class<?>, Object> CONTRACTS = new HashMap<>();

    public static <T> void register(Class<T> type, T impl) {
        CONTRACTS.put(type, impl);
    }

    public static <T> T get(Class<T> type) {
        return type.cast(CONTRACTS.get(type));
    }
}
