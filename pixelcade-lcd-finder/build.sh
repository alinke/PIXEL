mvn clean
mvn install && cd /Users/al/Documents/pixel/pixel/pixelcade-lcd-finder/target
cp /Users/al/Documents/pixel/pixel/pixelcade-lcd-finder/target/pixelcade-lcdfinder-1.0-jar-with-dependencies.jar /Users/al/Documents/pixel/pixel/pixelcade-lcd-finder/finder/pixelcadefinder.jar
cd /Users/al/Documents/pixel/pixel/pixelcade-lcd-finder/finder/
java -jar pixelcadefinder.jar
