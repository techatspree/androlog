/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
        Log.setDefaultLogLevel(Log.DEBUG);
        assertTrue(Log.isLoggable(tag, Log.DEBUG));

        Log.setDefaultLogLevel(Log.INFO);
        assertFalse(Log.isLoggable(tag, Log.DEBUG));
    }

    @Test
    public void testDeactivation() {
        String tag = "testLevel";
        Log.setDefaultLogLevel(Log.INFO);

        Log.activateLogging();
        assertTrue(Log.isLoggable(tag, Log.INFO));

        Log.deactivateLogging();
        assertFalse(Log.isLoggable(tag, Log.INFO));

        Log.activateLogging();
        assertTrue(Log.isLoggable(tag, Log.INFO));
    }

    @Test
    public void testActivationFromProperties() throws FileNotFoundException,
            IOException {
        File config = new File("src/test/resources/androlog-active.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        assertTrue(Log.isLoggable("any", Log.INFO));

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

        assertFalse(Log.isLoggable("any", Log.INFO));

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

        assertFalse(Log.isLoggable("any", Log.INFO));
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

        assertFalse(Log.isLoggable("any", Log.INFO));
    }

    @Test
    public void testLevels() throws FileNotFoundException, IOException {
        File config = new File("src/test/resources/androlog-levels.properties");

        Properties props = new Properties();
        props.load(new FileInputStream(config));
        Log.reset();
        Log.configure(props);

        // Default set to error
        assertFalse(Log.isLoggable("any", Log.INFO));
        assertTrue(Log.isLoggable("any", Log.ERROR));

        // Assert wrong level
        assertFalse(Log.isLoggable("my.log.invalid", Log.INFO));
        assertTrue(Log.isLoggable("my.log.invalid", Log.ERROR));

        // Check VERBOSE
        assertTrue(Log.isLoggable("my.log.verbose", Log.VERBOSE));
        assertTrue(Log.isLoggable("my.log.verbose2", Log.VERBOSE));

        // Check DEBUG
        assertFalse(Log.isLoggable("my.log.debug", Log.VERBOSE));
        assertFalse(Log.isLoggable("my.log.debug2", Log.VERBOSE));
        assertTrue(Log.isLoggable("my.log.debug", Log.DEBUG));
        assertTrue(Log.isLoggable("my.log.debug2", Log.DEBUG));

        // Check INFO
        assertFalse(Log.isLoggable("my.log.info", Log.DEBUG));
        assertFalse(Log.isLoggable("my.log.info2", Log.DEBUG));
        assertTrue(Log.isLoggable("my.log.info", Log.INFO));
        assertTrue(Log.isLoggable("my.log.info2", Log.INFO));

        // Check WARNING
        assertFalse(Log.isLoggable("my.log.warn", Log.INFO));
        assertFalse(Log.isLoggable("my.log.warn2", Log.INFO));
        assertTrue(Log.isLoggable("my.log.warn", Log.WARN));
        assertTrue(Log.isLoggable("my.log.warn2", Log.WARN));

        // Check ERROR
        assertFalse(Log.isLoggable("my.log.error", Log.WARN));
        assertFalse(Log.isLoggable("my.log.error2", Log.WARN));
        assertTrue(Log.isLoggable("my.log.error", Log.ERROR));
        assertTrue(Log.isLoggable("my.log.error2", Log.ERROR));

        // Check ASSERT
        assertFalse(Log.isLoggable("my.log.assert", Log.ERROR));
        assertFalse(Log.isLoggable("my.log.assert2", Log.ERROR));
        assertTrue(Log.isLoggable("my.log.assert", Log.ASSERT));
        assertTrue(Log.isLoggable("my.log.assert2", Log.ASSERT));
    }

}
