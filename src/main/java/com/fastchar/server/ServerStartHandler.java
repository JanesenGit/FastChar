package com.fastchar.server;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastPrinter;

public class ServerStartHandler {


    private Runnable startRunnable;

    private Runnable stopRunnable;

    public Runnable getStartRunnable() {
        return startRunnable;
    }

    private int port;
    private String contextPath;
    private String host;
    private final Thread shutdownHook = new Thread(ServerStartHandler.this::stop);

    public int getPort() {
        return port;
    }

    public ServerStartHandler setPort(int port) {
        this.port = port;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public ServerStartHandler setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ServerStartHandler setHost(String host) {
        this.host = host;
        return this;
    }

    public ServerStartHandler setStartRunnable(Runnable startRunnable) {
        this.startRunnable = startRunnable;
        return this;
    }

    public Runnable getStopRunnable() {
        return stopRunnable;
    }

    public ServerStartHandler setStopRunnable(Runnable stopRunnable) {
        this.stopRunnable = stopRunnable;
        return this;
    }

    public void start() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            Thread thread = new Thread(() -> {
                startRunnable.run();
                printInfo();
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (stopRunnable != null) {
            stopRunnable.run();
        }
    }


    private void printInfo() {
        FastPrinter printer = new FastPrinter();
        printer.info(this.getClass(), "Server startup successful! ");
        printer.info(this.getClass(), "ContextPath : " + this.contextPath);
        printer.info(this.getClass(), "Host : " + this.host);
        printer.info(this.getClass(), "Port : " + this.port);
    }

}
