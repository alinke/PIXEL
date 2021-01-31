cd ..
mvn clean
mvn install
cd /Users/al/pi/
rm pixelweb.jar
cp /Users/al/Documents/pixel/PIXEL/pixel-web-enabled/target/pixel-web-enabled-1.0-jar-with-dependencies.jar /Users/al/pi/pixelweb.jar
cd /Users/al/pi/
java -jar pixelweb.jar