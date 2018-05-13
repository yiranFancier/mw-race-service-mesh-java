package com.yiran.registry;

public class Endpoint {
    private final String host;
    private final int port;
    private final int loadLevel;
    private ServiceInfo supportedService;

    public Endpoint(String host,int port, int loadLevel){
        this.host = host;
        this.port = port;
        this.loadLevel = loadLevel;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return host + ":" + port;
    }

    public boolean equals(Object o){
        if (!(o instanceof Endpoint)){
            return false;
        }
        Endpoint other = (Endpoint) o;
        return other.host.equals(this.host) && other.port == this.port;
    }

    public int hashCode(){
        return host.hashCode() + port;
    }

    public ServiceInfo getSupportedService() {
        return supportedService;
    }

    public void setSupportedService(ServiceInfo supportedService) {
        this.supportedService = supportedService;
    }

    public int getLoadLevel() {
        return loadLevel;
    }
}
