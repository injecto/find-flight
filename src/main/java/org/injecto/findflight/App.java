package org.injecto.findflight;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class App {
    private final Server server;

    @Inject
    public App(Server server) {
        this.server = server;
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
        server.stop();
    }
}
