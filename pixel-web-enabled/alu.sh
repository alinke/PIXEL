#cd ..
mvn clean
mvn install
cd /Users/al/pixelcade/
rm pixelweb.jar
cp /Users/al/Documents/pixel/PIXEL/pixel-web-enabled/target/pixel-web-enabled-1.0-jar-with-dependencies.jar /Users/al/pixelcade/pixelweb.jar
cp /Users/al/Documents/pixel/PIXEL/pixel-web-enabled/target/pixel-web-enabled-1.0-jar-with-dependencies.jar /Users/al/Documents/windows-shared/jsmooth/pixelweb.jar
cd /Users/al/pixelcade/
#java -jar pixelweb.jar
ssh-keygen -R 192.168.1.144
scp pixelweb.jar root@192.168.1.144:/opt/pixelcade
