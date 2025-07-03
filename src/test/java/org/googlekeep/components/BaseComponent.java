package org.googlekeep.components;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Base class for all UI components used on pages.
 * Provides common functionality for interacting with elements.
 */
public abstract class BaseComponent {
    protected WebDriver driver;
    protected WebElement root;

    public BaseComponent(WebDriver driver, WebElement root) {
        this.driver = driver;
        this.root = root;
    }

    /**
     * Returns the root element of the component.
     */
    public WebElement getRoot() {
        return root;
    }

    /**
     * Returns the driver instance.
     */
    public WebDriver getDriver() {
        return driver;
    }
}