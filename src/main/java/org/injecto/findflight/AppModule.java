package org.injecto.findflight;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        Properties properties = new Properties();
        try {
            InputStream cfgStream = getClass().getResourceAsStream("/config.properties");
            if (cfgStream == null)
                throw new RuntimeException("Can't find `config.properties`");

            properties.load(cfgStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't load properties");
        }
        Names.bindProperties(binder(), properties);
    }

    @Provides
    @Singleton
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
