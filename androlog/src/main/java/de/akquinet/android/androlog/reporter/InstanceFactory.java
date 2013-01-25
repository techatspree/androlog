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


/**
 * Helper class to create instances from
 * class names.
 */
public class InstanceFactory {

    /**
     * Creates the Reporter instance. This method loads the class and then
     * instantiates the Reporter object.
     * @param clazzName the class name
     * @return the Reporter object
     * @throws RuntimeException if the class cannot be found
     */
    public static Reporter newReporter(String clazzName) {
        return newInstance(Reporter.class, clazzName);
    }

    /**
     * Creates a ReportFactory instance, loading the class first.
     * @param clazzName the class name
     * @return the {@link ReportFactory} object
     * @throws RuntimeException if the class cannot be found
     */
    public static ReportFactory newReportFactory(String clazzName) {
        return newInstance(ReportFactory.class, clazzName);
    }

    private static <T> T newInstance(Class<T> baseClazz, String clazzName) {
        try {
            Class<T> clazz = load(baseClazz, clazzName);
            return clazz.newInstance();
        } catch (Exception e) {
            // Any exception is critical here.
            throw new RuntimeException("Cannot load class " + clazzName);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> load(Class<T> baseClazz, String clazzName) throws ClassNotFoundException {
        ClassLoader loader = baseClazz.getClassLoader();
        try {
            return (Class<T>) loader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            loader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) loader.loadClass(clazzName);
        }
    }
}
