Androlog is a wrapper on the top of the Androlog Log system allowing:

* The enabling / disabling of the logging
* Configuring the log level per ``tag``
* Logging message with simpler method (no tag required)
* Configuring the logging per application
* Reporting issue from your application

Androlog supports Android 1.6+ and is licensed under the Apache License 2.0. This project is founded by [[akquinet A.G.|http://akquinet.de/en]].

Motivations
===========
When you're developing Android applications, you generally use the [[Android logger|http://developer.android.com/reference/android/util/Log.html]](Android Logger API). However, this logging system is too simplistic to meet sophisticated application development requirement:

* All log messages are displaying in the console (logcat), regardless the log level and the tag
* There is no way to disable the logging easily without removing all ``Log.x`` calls. However **this is required before uploading your application in the marketplace**.
* Android reporting was introduced in Android 2+, and is quite limited. Reports just contains the stack trace and a user message. Androlog reporting works on 1.6,  contains the logged messages and stack traces. So Androlog reports allow a better understanding of the issues.

Download
=========
You can download the Androlog sources from github. We also provides binaries for convenience:
* [[androlog-1.0.0.jar|http://repo2.maven.org/maven2/de/akquinet/android/androlog/androlog/1.0.0/androlog-1.0.0.jar]] (just add this jar to your build path)
* Maven dependency (to use with the maven-android-plugin)

```xml
<dependency>
  <groupId>de.akquinet.android.androlog</groupId> 
  <artifactId>androlog</artifactId> 
  <version>1.0.0</version> 
  <scope>compile</scope>
</dependency>
```

Androlog is available on maven central, so you don't need to customize your maven settings.

Brief description
=================
Androlog is a pretty small library that you can embed in your Android application. Instead of calling ``android.util.Log``, you just use ``de.akquinet.android.androlog.Log`` (a simple replace on the import statements works). The provided API is similar to the Android Log. So androlog provides the same log levels : ``VERBOSE``, ``DEBUG``, ``INFO``, ``WARN``, and ``ERROR`` with the same methods : ``Log.v``, ``Log.d``, ``Log.i``, ``Log.w``, ``Log.e``. 

The following code snippet use Androlog:

```java
import de.akquinet.android.androlog.Log;

public class AndrologExample {
    public static final String TAG = "MY_TAG";
    public void doSomething() {
        Log.i(TAG, "A message");
    }
}
```

As you can see it is really similar to the Android log. However, this logging system can be configured to:

* disabled all messages (for releases)
* configure the log level per TAG,
* configure a default log level
* configure different logging policy per application

Documentation
=============

* [[androlog Quick Start|Androlog-quickstart]]
* [[The Log API|Androlog-api]]
* [[The Log configuration|Androlog-configuration]]
* [[The Androlog Reporting|Androlog-reporting]]
* [[The Androlog developer guide|Androlog-developer]]
* [[Javadoc|http://akquinet.github.com/androlog/javadoc]]


