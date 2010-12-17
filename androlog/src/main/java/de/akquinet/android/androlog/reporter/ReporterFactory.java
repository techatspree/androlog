package de.akquinet.android.androlog.reporter;


/**
 * Helper class to create Reporter instance from
 * class names.
 */
public class ReporterFactory {


    /**
     * Loads the Reporter Class.
     * @param clazzName the class name
     * @return the Reporter Class object
     * @throws ClassNotFoundException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static Class<Reporter> load(String clazzName) throws ClassNotFoundException {
        ClassLoader loader = Reporter.class.getClassLoader();
        try {
            return (Class<Reporter>) loader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            loader = Thread.currentThread().getContextClassLoader();
            return (Class<Reporter>) loader.loadClass(clazzName);
        }
    }

    /**
     * Creates the Reporter instance. This method loads the class and then
     * instantiates the Reporter object.
     * @param clazzName the class name
     * @return the Reporter object
     * @throws RuntimeException if the class cannot be found
     */
    public static Reporter newInstance(String clazzName) {
        Class<Reporter> clazz = null;
        try {
            clazz = load(clazzName);
            return clazz.newInstance();
        } catch (Exception e) {
            // Any exception is critical here.
            throw new RuntimeException("Cannot load class " + clazzName);
        }
    }

}
