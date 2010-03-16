ant deps
ant jar
#mvn install:install-file -DgroupId=org.clojure -DartifactId=compojure -Dversion=1.0-SNAPSHOT -Dfile=compojure.jar -Dpackaging=jar
mvn install:install-file -DpomFile=pom.xml -Dfile=compojure.jar

echo "REMOVE THESE TWO: grizzyly-{webserver,servlet} they're in some maven repo somewhere..."
mvn install:install-file -DgroupId=com.sun.grizzly -DartifactId=grizzly-http-servlet -Dversion=1.9.10 -Dpackaging=jar -Dfile=deps/grizzly-http-servlet-1.9.10.jar

mvn install:install-file -DgroupId=com.sun.grizzly -DartifactId=grizzly-http-webserver -Dversion=1.9.10 -Dpackaging=jar -Dfile=deps/grizzly-http-webserver-1.9.10.jar

# deps/clojure-contrib.jar
# deps/clojure.jar
# deps/commons-codec-1.3.jar
# deps/commons-fileupload-1.2.1.jar
# deps/commons-io-1.4.jar
# deps/grizzly-http-servlet-1.9.10.jar
# deps/grizzly-http-webserver-1.9.10.jar
# deps/jetty-6.1.15.jar
# deps/jetty-util-6.1.15.jar
# deps/servlet-api-2.5-20081211.jar
