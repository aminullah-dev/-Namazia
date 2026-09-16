#!/bin/sh
#
# Gradle wrapper script
#

DIRNAME="$(dirname "$0")"
CLASSPATH="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_HOME=""

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
