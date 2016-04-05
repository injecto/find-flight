package org.injecto.findflight;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class App {
    private final Server server;
    private final ExecutorService executorService;

    @Inject
    public App(Server server, ExecutorService executorService) {
        this.server = server;
        this.executorService = executorService;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        App app = injector.getInstance(App.class);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        server.stop();
    }
}
