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

import android.content.Context;

/**
 * Specialisation of {@link Reporter} for use in conjunction with a configured {@link ReportFactory}
 */
public interface EnhancedReporter extends Reporter {

    /**
     * Asks the reporter to send a report. Called with a new {@link Reporter} instance for each reporter (but reporters
     * shouldn't modify the report anyway).
     * 
     * @param context
     *            the Android context
     * @param report
     *            the report
     * @return <code>true</code> if the report was sent correctly, <code>false</code> otherwise.
     */
    public abstract boolean send(Context context, Report report);
}
