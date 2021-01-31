mvn clean
mvn install
cd /Users/mina/pixelc/
rm pixelweb.jar
cp /Users/mina/code/PIXEL/pixel-web-enabled/target/pixel-web-enabled-0.0.1-SNAPSHOT-jar-with-dependencies.jar /Users/mina/pixelc/pixelweb.jar
java -jar pixelweb.jar