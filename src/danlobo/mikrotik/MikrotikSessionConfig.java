package danlobo.mikrotik;

public class MikrotikSessionConfig {
    private int _connectTimeout;
    private int _responseTimeout;

    public int getResponseTimeout() {
        return _responseTimeout;
    }

    public void setResponseTimeout(int value) {
        this._responseTimeout = value;
    }

    public int getConnectTimeout() {
        return _connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this._connectTimeout = connectTimeout;
    }

    public MikrotikSessionConfig() {
        _connectTimeout = 15000;
        _responseTimeout = 30000;
    }
}
