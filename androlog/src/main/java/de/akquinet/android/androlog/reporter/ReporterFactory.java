package de.akquinet.android.androlog.reporter;


public class ReporterFactory {


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
