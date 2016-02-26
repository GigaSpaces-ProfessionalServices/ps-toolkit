package com.gigaspaces.gigapro.web.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private final String url;
    private static final Logger LOG = LoggerFactory.getLogger(BrowserLauncher.class);
    
    private static final String OS_NAME_KEY = "os.name";
    
    public BrowserLauncher(String url) {
        Assert.notNull(url, "'url' parameter must not be null or empty");
        this.url = url;
    }
    
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (Desktop.isDesktopSupported()) {
            runDefaultBrowser();
        } else {
            String osName = System.getProperty(OS_NAME_KEY, "").toLowerCase(Locale.ENGLISH);
            if (osName.indexOf("nux") != -1) {
                runDefaultBrowserOnUnix();
            } else if (osName.indexOf("win") != -1) {
                runDefaultBrowserOnWindows();
            } else {
                LOG.warn("Unsupported OS name: " + osName + ". Failed to launch user default browser."); 
            }
        }
    }
    
    private void runDefaultBrowser() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(url));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL = " + url + " is invalid");
        } catch (IOException e) {
            LOG.warn("Failed to launch user default browser.", e);
        }
    }
    
    private void runDefaultBrowserOnUnix() {
        run("xdg-open " + url);
    }
    
    private void runDefaultBrowserOnWindows() {
        run("cmd /c start " + url);
    }
    
    private void run(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            LOG.warn("Failed to launch " + command, e);
        }
    }
}
