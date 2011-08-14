/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.android.androlog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class LogTest {
    private Properties configuration;

    @Before
    public void setUp() {
        configuration = new Properties();
        Log.configure(configuration);
    }

    @Test
    public void testDefaultLogLevel() {
        String tag = "testLevel";
        Log.setDefaultLogLevel(Constants.DEBUG);
        assertTrue(Log.isLoggable(tag, Constants.DEBUG));

        Log.setDefaultLogLevel(Constants.INFO);
        assertFalse(Log.isLoggable(tag, Constants.DEBUG));

        // Test with this
        Log.setDefaultLogLevel(Constants.DEBUG);
        assertTrue(Log.isLoggable(this, Constants.DEBUG));

        Log.setDefaultLogLevel(Constants.INFO);
        assertFalse(Log.isLoggable(this, Constants.DEBUG));
    }

    @Test
    public void testDeactivation() {
        String tag = "testLevel";
        Log.setDefaultLogLevel(Constants.INFO);

        Log.activateLogging();
        assertTrue(Log.isLoggable(tag, Constants.INFO));

        Log.deactivateLogging();
        assertFalse(Log.isLoggable(tag, Constants.INFO));

        Log.activateLogging();
        assertTrue(Log.isLoggable(tag, Constants.INFO));

        // Test with this
        Log.activateLogging();
        assertTrue(Log.isLoggable(this, Constants.INFO));

        Log.deactivateLogging();
        assertFalse(Log.isLoggable(this, Constants.INFO));

        Log.activateLogging();
        assertTrue(Log.isLoggable(this, Constants.INFO));
    }

    @Test
    public void testActivationFromProperties() throws FileNotFoundException,
            IOException {
        File config = new File("src/test/resources/androlog-active.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        assertTrue(Log.isLoggable("any", Constants.INFO));
        assertTrue(Log.isLoggable(this, Constants.INFO));

    }

    @Test
    public void testDeactivationFromProperties() throws FileNotFoundException,
            IOException {
        File config = new File(
                "src/test/resources/androlog-disabled.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        assertFalse(Log.isLoggable("any", Constants.INFO));
        assertFalse(Log.isLoggable(this, Constants.INFO));

    }

    @Test
    public void testMissingActiveFromProperties() throws FileNotFoundException,
            IOException {
        File config = new File(
                "src/test/resources/androlog-active-missing.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        assertFalse(Log.isLoggable("any", Constants.INFO));
        assertFalse(Log.isLoggable(this, Constants.INFO));

    }

    @Test
    public void testCorruptedActiveFromProperties()
            throws FileNotFoundException, IOException {
        File config = new File(
                "src/test/resources/androlog-corrupted.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        assertFalse(Log.isLoggable("any", Constants.INFO));
        assertFalse(Log.isLoggable(this, Constants.INFO));

    }

    @Test
    public void testLevels() throws FileNotFoundException, IOException {
        File config = new File("src/test/resources/androlog-levels.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        // Default set to error
        assertFalse(Log.isLoggable("any", Constants.INFO));
        assertTrue(Log.isLoggable("any", Constants.ERROR));
        assertFalse(Log.isLoggable(this, Constants.INFO));
        assertTrue(Log.isLoggable(this, Constants.ERROR));

        // Assert wrong level
        assertFalse(Log.isLoggable("my.log.invalid", Constants.INFO));
        assertTrue(Log.isLoggable("my.log.invalid", Constants.ERROR));

        // Check VERBOSE
        assertTrue(Log.isLoggable("my.log.verbose", Constants.VERBOSE));
        assertTrue(Log.isLoggable("my.log.verbose2", Constants.VERBOSE));

        // Check DEBUG
        assertFalse(Log.isLoggable("my.log.debug", Constants.VERBOSE));
        assertFalse(Log.isLoggable("my.log.debug2", Constants.VERBOSE));
        assertTrue(Log.isLoggable("my.log.debug", Constants.DEBUG));
        assertTrue(Log.isLoggable("my.log.debug2", Constants.DEBUG));

        // Check INFO
        assertFalse(Log.isLoggable("my.log.info", Constants.DEBUG));
        assertFalse(Log.isLoggable("my.log.info2", Constants.DEBUG));
        assertTrue(Log.isLoggable("my.log.info", Constants.INFO));
        assertTrue(Log.isLoggable("my.log.info2", Constants.INFO));

        // Check WARNING
        assertFalse(Log.isLoggable("my.log.warn", Constants.INFO));
        assertFalse(Log.isLoggable("my.log.warn2", Constants.INFO));
        assertTrue(Log.isLoggable("my.log.warn", Constants.WARN));
        assertTrue(Log.isLoggable("my.log.warn2", Constants.WARN));

        // Check ERROR
        assertFalse(Log.isLoggable("my.log.error", Constants.WARN));
        assertFalse(Log.isLoggable("my.log.error2", Constants.WARN));
        assertTrue(Log.isLoggable("my.log.error", Constants.ERROR));
        assertTrue(Log.isLoggable("my.log.error2", Constants.ERROR));

        // Check ASSERT
        assertFalse(Log.isLoggable("my.log.assert", Constants.ERROR));
        assertFalse(Log.isLoggable("my.log.assert2", Constants.ERROR));
        assertTrue(Log.isLoggable("my.log.assert", Constants.ASSERT));
        assertTrue(Log.isLoggable("my.log.assert2", Constants.ASSERT));
    }

    @Test
    public void testIsAssertLoggable() {
        assertTrue(Log.isLoggable("any", Constants.ASSERT));
        Log.deactivateLogging();
        assertTrue(Log.isLoggable("any", Constants.ASSERT));
        assertTrue(Log.isLoggable(this, Constants.ASSERT));
        Log.activateLogging();
        assertTrue(Log.isLoggable("any", Constants.ASSERT));
        assertTrue(Log.isLoggable(this, Constants.ASSERT));
    }

}
