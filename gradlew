#!/bin/sh
#
# Gradle startup script for UNIX
#

# ──────────────────────────────────────────────────────────────────────────────
# Attempt to set APP_HOME

PRG="$0"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG="$(dirname "$PRG")/$link"
    fi
done
APP_HOME=$(dirname "$PRG")

# Find java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and 'java' was not found in PATH."
    echo "Please install Java 25+ and set JAVA_HOME."
    exit 1
fi

exec "$JAVACMD" \
    -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    "$@"
