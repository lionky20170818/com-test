#!/bin/sh
PRG="$0"

JAR_FILE=tradeprocess-web-1.0-SNAPSHOT.jar

DEBUG="-Xms128m -Xmx512m -Xdebug -Xrunjdwp:transport=dt_socket,address=4380,server=y,suspend=n"
JVM_ARGS="-server -Xms256m -Xmx1024m -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseCMSCompactAtFullCollection"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`


[ -z "$APP_HOME" ] && APP_HOME=`cd "$PRGDIR" >/dev/null; pwd`
[ -z "$APP_PID" ] && APP_PID=$APP_HOME/pid


if [ ! -f "$APP_PID" ]; then
        java $JVM_ARGS $DEBUG -jar $APP_HOME/$JAR_FILE  2>&1 &
        PID=$!
        echo $PID > $APP_PID
else
        PID=`cat $APP_PID`
        echo "server is running with pid $PID."
fi

