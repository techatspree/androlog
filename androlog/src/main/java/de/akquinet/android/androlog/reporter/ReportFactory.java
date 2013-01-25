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

import de.akquinet.android.androlog.Log;
import android.content.Context;

/**
 * Factory for building {@link Report}s customised with additional application & device properties
 */
public interface ReportFactory {
    /**
     * Create a {@link Report} from the given parameters
     * 
     * @param context
     *            the Android cocntext
     * @param message
     *            message passed to {@link Log#report(String, Throwable)}, or <code>null</code> if {@link Log#report()}
     *            was called
     * @param throwable
     *            throwable passed to {@link Log#report(String, Throwable)}, or <code>null</code> if
     *            {@link Log#report()} was called
     * @return the report to be sent via a configured {@link EnhancedReporter}.
     */
    public abstract Report create(Context context, String message, Throwable throwable);
}
