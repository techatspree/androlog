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
package de.akquinet.android.androlog.reporter;

import java.util.Properties;

import de.akquinet.android.androlog.Log;

import android.content.Context;

/**
 * Reporter Interface.
 * A Reporter sends a Report to a specific endpoint
 * (url, mail ...).
 * Reporter instances are created by {@link Log} according
 * to the {@link Log#ANDROLOG_REPORT_REPORTERS} configuration
 * property.
 */
public interface Reporter {

    /**
     * Configures the reporter.
     * Each reporter can define their own configuration properties.
     * @param configuration the Androlog configuration.
     */
    public abstract void configure(Properties configuration);

    /**
     * Asks the reporter to send the report.
     * @param context the Android context
     * @param message a message (optional)
     * @param error a error to attach to the report (optional)
     * @return <code>true</code> if the report was sent correctly,
     * <code>false</code> otherwise.
     */
    public abstract boolean send(Context context, String message, Throwable error);



}