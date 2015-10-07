package danlobo.mikrotik;

public class MikrotikSessionConfig {
    int connectTimeout;
    int responseTimeout;

    public MikrotikSessionConfig() {
        connectTimeout = 15000;
        responseTimeout = 30000;
    }
}
