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

import android.content.Context;

/**
 * Dummy Reporter doing nothing...
 */
public class NoopReporter implements Reporter {

    /**
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {
        // Do nothing.
    }

    /**
     * @see de.akquinet.android.androlog.reporter.Reporter#send(android.content.Context,
     *      java.lang.String, java.lang.Throwable)
     */
    @Override
    public boolean send(Context context, String message, Throwable error) {
        // Always return true.
        return true;
    }

}
