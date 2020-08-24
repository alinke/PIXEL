#!/bin/bash

java_installed=false
install_succesful=false
lcd_marquee=false
black=`tput setaf 0`
red=`tput setaf 1`
green=`tput setaf 2`
yellow=`tput setaf 3`
blue=`tput setaf 4`
magenta=`tput setaf 5`
white=`tput setaf 7`
reset=`tput sgr0`

cat << "EOF"
       _          _               _
 _ __ (_)_  _____| | ___ __ _  __| | ___
| '_ \| \ \/ / _ \ |/ __/ _` |/ _` |/ _ \
| |_) | |>  <  __/ | (_| (_| | (_| |  __/
| .__/|_/_/\_\___|_|\___\__,_|\__,_|\___|
|_|
EOF

echo "${magenta}  Pixelcade Art Pack 1 Installer   ${white}"
echo ""
echo "${red}IMPORTANT:${white} This script will work on a Pi 2, Pi Zero W, Pi 3B, Pi 3B+, and Pi 4"
echo "Now connect Pixelcade to a free USB port on your Pi (directly connected to your Pi or use a powered USB hub)"
echo "Ensure the toggle switch on the Pixelcade board is pointing towards USB and not BT"
echo "If you have PixelcadeLCD, then ensure PixelcadeLCD is connected to your Pi with a microUSB cable"


if [ ! -d "$HOME/pixelcade" ]
then
    echo "${yellow}Pixelcade is not installed${white}"
    echo "${yellow}Please install Pixelcade from http://pixelcade.org first and then re-run this installer${white}"
    exit 1
fi

if type -p java ; then
  echo "${yellow}Java already installed, skipping..."
  java_installed=true
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
  echo "${yellow}Java already installed, skipping..."
  java_installed=true
else
   echo "${yellow}Java is not installed, please install Pixelcade for Pi first at http://pixelcade.org...${white}"
   java_installed=false
   exit 1
fi

while true; do
       read -p "${magenta}Do you have PixelcadeLCD? (y/n)? ${white}" yn
       case $yn in
           [Yy]* ) lcd_marquee=true; break;;
           [Nn]* ) lcd_marquee=false; break;;
           * ) echo "Please answer y or n";;
       esac
done

if [ "$lcd_marquee" = true ] ; then
  echo "${yellow}Downloading PixelcadeLCD Marquee Files...${white}"
  cd $HOME
  curl -LO pixelcade.org/pi/591333.jar
fi

# we have all the pre-requisites so let's continue
sudo apt-get -y update

echo "${yellow}Installing Git...${white}"
sudo apt -y install git

# let's delete the art pack if already there and download new so we have the latest and greatest
if [[ -d "$HOME/pixelcade-artpack" ]]; then
  echo "${yellow}Removing Existing Art Pack...${white}"
  cd $HOME && sudo rm -r pixelcade-artpack
fi

#this java program will prompt for serial code and then prompt for LED and LCD marquees
echo "${green}Starting Download...${green}"
cd $HOME
curl -LO pixelcade.org/pi/222111.jar
java -jar 222111.jar

# now let's cleanup
if [[ -d "$HOME/pixelcade-artpack" ]]; then
  echo "${yellow}Cleaning Up...${white}"
  sudo rm -r $HOME/pixelcade-artpack && rm $HOME/591333.jar & rm $HOME/222111.jar
fi

install_succesful=true

if [ "$install_succesful" = true ] ; then
  while true; do
      read -p "${magenta}You'll need to Reboot, ok to Reboot Now? (y/n)${white}" yn
      case $yn in
          [Yy]* ) sudo reboot; break;;
          [Nn]* ) echo "${yellow}Please reboot when you get a chance, thanks" && exit;;
          * ) echo "Please answer yes or no.";;
      esac
  done
fi
