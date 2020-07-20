#!/bin/bash
stretch_os=false
buster_os=false
ubuntu_os=false
retropie=false
pizero=false
pi4=false
aarch64=false
java_installed=false
install_succesful=false
auto_update=false
lcd_marquee=false
led_marquee=false
attractmode=false
black=`tput setaf 0`
red=`tput setaf 1`
green=`tput setaf 2`
yellow=`tput setaf 3`
blue=`tput setaf 4`
magenta=`tput setaf 5`
white=`tput setaf 7`
reset=`tput sgr0`
version=4  #increment this as the script is updated
#echo "${red}red text ${green}green text${reset}"

cat << "EOF"
       _          _               _
 _ __ (_)_  _____| | ___ __ _  __| | ___
| '_ \| \ \/ / _ \ |/ __/ _` |/ _` |/ _ \
| |_) | |>  <  __/ | (_| (_| | (_| |  __/
| .__/|_/_/\_\___|_|\___\__,_|\__,_|\___|
|_|
EOF

echo "${magenta}       Pixelcade for RetroPie : Installer Version $version    ${white}"
echo ""
echo "${red}IMPORTANT:${white} This script will work on a Pi 2, Pi Zero W, Pi 3B, Pi 3B+, and Pi 4"
echo "Now connect Pixelcade to a free USB port on your Pi (directly connected to your Pi or use a powered USB hub)"
echo "Ensure the toggle switch on the Pixelcade board is pointing towards USB and not BT"


#while true; do
#    read -p "${magenta}Would you like to enable auto updates (y/n)? ${white}" yn
#    case $yn in
#        [Yy]* ) auto_update=true; break;;
#        [Nn]* ) auto_update=false; break;;
#        * ) echo "Please answer y or n";;
#    esac
#done

auto_update=false

#add a prompt here if user really wants to install again

# let's check the version and only proceed if the user has an older version
if [[ -d "$HOME/pixelcade" ]]; then
    if [[ -f "$HOME/pixelcade/pixelcade-version" ]]; then
      echo "Existing Pixelcade installation detected, checking version..."
      read -r currentVersion<$HOME/pixelcade/pixelcade-version
      if [[ $currentVersion -lt $version ]]; then
            echo "Older Pixelcade version detected, now upgrading..."
        else
            while true; do
                read -p "${magenta}Your Pixelcade version is already up to date. If you continue, your Pixelcade installation will be deleted including any custom artwork you've added, do you want to re-install? Or select n to just check for new artwork. (y/n) ${white}" yn
                case $yn in
                    [Yy]* ) cd $HOME && sudo rm -r pixelcade; break;;
                    [Nn]* ) cd $HOME/pixelcade/system && sh ./update.sh && exit; break;;
                    * ) echo "Please answer y or n";;
                esac
            done
      fi
    else
       while true; do
           read -p "${magenta}Your existing Pixelcade installation will be deleted including any custom artwork you've added, do you want to re-install? Or select n to just check for new artwork. (y/n) ${white}" yn
           case $yn in
               [Yy]* ) cd $HOME && sudo rm -r pixelcade; break;;
               [Nn]* ) cd $HOME/pixelcade/system && sh ./update.sh && exit; break;;
               * ) echo "Please answer y or n";;
           esac
       done
    fi
fi
#add prompt to remove existing pixelcade folder


# detect what OS we have
if lsb_release -a | grep -q 'stretch'; then
   echo "${yellow}Linux Stretch Detected${white}"
   stretch_os=true
elif lsb_release -a | grep -q 'buster'; then
   echo "${yellow}Linux Buster Detected${white}"
   buster_os=true
elif lsb_release -a | grep -q 'ubuntu'; then
    echo "${yellow}Ubuntu Linux Detected${white}"
    ubuntu_os=true
    echo "Installing curl..."
    sudo apt install curl
else
   echo "${red}Sorry, neither Linux Stretch, Linux Buster, or Ubuntu were detected, exiting..."
   exit 1
fi

# let's detect if Pixelcade is connected
if ls /dev/ttyACM0 | grep -q '/dev/ttyACM0'; then
   echo "${yellow}Pixelcade LED Marquee Detected${white}"
else
   echo "${red}Sorry, Pixelcade LED Marquee was not detected, pleasse ensure Pixelcade is USB connected to your Pi and the toggle switch on the Pixelcade board is pointing towards USB, exiting..."
   exit 1
fi

#let's check if retropie is installed
if [[ -f "/opt/retropie/configs/all/autostart.sh" ]]; then
  echo "RetroPie installation detected..."
  retropie=true
else
   echo "${yellow}RetroPie is not installed..."
fi

if cat /proc/device-tree/model | grep -q 'Pi 4'; then
   echo "${yellow}Raspberry Pi 4 detected..."
   pi4=true
fi

if uname -m | grep -q 'aarch64'; then
   echo "${yellow}aarch64 detected or ARM 64-bit..."
   aarch64=true
fi

if cat /proc/device-tree/model | grep -q 'Pi Zero W'; then
   echo "${yellow}Raspberry Pi Zero detected..."
   pizero=true
fi

#while true; do
#    read -p "${magenta}Do you have a PixelcadeLCD Marquee (y/n)? ${white}" yn
#    case $yn in
#        [Yy]* ) lcd_marquee=true; break;;
#        [Nn]* ) lcd_marquee=false; break;;
#        * ) echo "Please answer y or n";;
#    esac
#done

lcd_marquee=true

if type -p java ; then
  echo "${yellow}Java already installed, skipping..."
  java_installed=true
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
  echo "${yellow}Java already installed, skipping..."
  java_installed=true
else
   echo "${yellow}Java not found, let's install Java...${white}"
   java_installed=false
fi

# we have all the pre-requisites so let's continue
sudo apt-get -y update

if [ "$java_installed" = false ] ; then #only install java if it doesn't exist
    if [ "$pizero" = true ] ; then
      echo "${yellow}Installing Zulu Java 8...${white}"
      sudo mkdir /opt/jdk/
      cd /opt/jdk
      sudo curl -LO http://pixelcade.org/pi/zulu8.46.0.225-ca-jdk8.0.252-linux_aarch32hf.tar.gz
      sudo tar -xzvf zulu8.46.0.225-ca-jdk8.0.252-linux_aarch32hf.tar.gz
      sudo update-alternatives --install /usr/bin/java java /opt/jdk/zulu8.46.0.225-ca-jdk8.0.252-linux_aarch32hf/bin/java 252
      sudo update-alternatives --install /usr/bin/javac javac /opt/jdk/zulu8.46.0.225-ca-jdk8.0.252-linux_aarch32hf/bin/javac 252
    elif [ "$stretch_os" = true ]; then
       echo "${yellow}Installing Java 8...${white}"
       sudo apt-get -y install oracle-java8-jdk
    elif [ "$buster_os" = true ]; then #pi zero is arm6 and cannot run the normal java :-( so have to get this special one
       echo "${yellow}Installing Small JRE 11 for aarch32...${white}"
       #sudo apt-get -y install openjdk-11-jre //older larger jre but we want smaller instead
       sudo mkdir /usr/lib/jvm && sudo mkdir /usr/lib/jvm/jre11-aarch32 && cd /usr/lib/jvm/jre11-aarch32
       sudo curl -LO https://github.com/alinke/small-jre/raw/master/jre11-aarch32.tar.gz
       sudo tar -xzvf jre11-aarch32.tar.gz
       sudo rm jre11-aarch32.tar.gz
       sudo chmod +x /usr/lib/jvm/jre11-aarch32/bin/java #actually this should already be +x but just in case
       sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jre11-aarch32/bin/java 11
    elif [ "$ubuntu_os" = true ]; then
        echo "${yellow}Installing Java OpenJDK 11...${white}"
        sudo apt-get -y install openjdk-11-jre
    elif [ "$aarch64" = true ]; then
        echo "${yellow}Installing Small JRE 11 for aarch64...${white}"
        #sudo apt-get -y install openjdk-11-jre
        sudo mkdir /usr/lib/jvm/jre11-aarch64 && cd /usr/lib/jvm/jre11-aarch64
        sudo curl -LO https://github.com/alinke/small-jre/raw/master/jre11-aarch64.tar.gz
        sudo tar -xzvf jre11-aarch64.tar.gz
        sudo rm jre11-aarch64.tar.gz
        sudo chmod +x /usr/lib/jvm/jre11-aarch64/bin/java #actually this should already be +x but just in case
        sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jre11-aarch64/bin/java 11
    else
        echo "${red}Sorry, neither Linux Stretch or Linux Buster was detected, exiting..."
        exit 1
    fi
fi

echo "${yellow}Installing Git...${white}"
sudo apt -y install git

# this is where pixelcade will live

echo "${yellow}Installing Pixelcade from GitHub Repo...${white}"
cd $HOME
git clone --depth 1 https://github.com/alinke/pixelcade.git
cd $HOME/pixelcade
sudo chmod +x pixelweb
git config user.email "sample@sample.com"
git config user.name "sample"

if [ "$lcd_marquee" = true ] ; then
  sudo apt -y install qt5-default
  sudo apt -y install libqt5qml5
  sudo apt -y install libqt5quickcontrols2-5
  sudo apt -y install qml-module-qtquick2
  sudo apt -y install qml-module-qtquick-controls
  sudo apt -y install qml-module-qt-labs-platform
  sudo apt -y install qml-module-qtquick-extras
  sudo chmod +x $HOME/pixelcade/skrola
  sudo chmod +x $HOME/pixelcade/gsho
  echo "${yellow}Changing the default font for the LCD Marquee...${white}"
  sudo sed -i 's/^LCDMarquee=no/LCDMarquee=yes/g' $HOME/pixelcade/settings.ini
  sudo sed -i 's/^font=Arial Narrow 7/font=Vectroid/g' $HOME/pixelcade/settings.ini
  #echo "${yellow}Modifying /boot/config.txt for dual monitor support...${white}"
fi

#if [ "$retropie" = true ] ; then  #skip if no retropie as we'll start this later using systemd
#    cd $HOME/pixelcade
#    #./pixelweb -b & #run pixelweb in the background\
#    ./pixelweb -b &
#fi

cd $HOME
#if retropie is present, add our mods
if [ "$retropie" = true ] ; then
  # lets install the correct mod based on the OS
  if [ "$pi4" = true ] ; then
      curl -LO http://pixelcade.org/pi/esmod-pi4.deb && sudo dpkg -i esmod-pi4.deb
  elif [ "$stretch_os" = true ] ; then
      curl -LO http://pixelcade.org/pi/esmod-stretch.deb && sudo dpkg -i esmod-stretch.deb
  elif [ "$buster_os" = true ]; then
      curl -LO http://pixelcade.org/pi/esmod-buster.deb && sudo dpkg -i esmod-buster.deb
  elif [ "$ubuntu_os" = true ]; then
      curl -LO http://pixelcade.org/pi/esmod-ubuntu.deb && sudo dpkg -i esmod-ubuntu.deb
  else
      echo "${red}Sorry, neither Linux Stretch, Linux Buster, or Ubuntu was detected, exiting..."
      exit 1
  fi
fi

#now lets check if the user also has attractmode installed

if [[ -d "/$HOME/.attract" ]]; then
  echo "Attract Mode front end detected, installing Pixelcade plug-in for Attract Mode..."
  attractmode=true
  cd $HOME
  if [[ -d "$HOME/pixelcade-attract-mode" ]]; then
    sudo rm -r $HOME/pixelcade-attract-mode
    git clone https://github.com/tnhabib/pixelcade-attract-mode.git
  else
    git clone https://github.com/tnhabib/pixelcade-attract-mode.git
  fi
  sudo cp -r $HOME/pixelcade-attract-mode/Pixelcade $HOME/.attract/plugins
else
  attractmode=false
  echo "${yellow}Attract Mode front end is not installed..."
fi

#get the pixelcade startup-up script
#note this file is not in the git repo because we're going to make a change locally
echo "${yellow}Configuring Pixelcade Startup Script...${white}"
cd $HOME/pixelcade/system
curl -LO http://pixelcade.org/pi/pixelcade-startup.sh
sudo chmod +x $HOME/pixelcade/system/pixelcade-startup.sh
curl -LO http://pixelcade.org/pi/update.sh
sudo chmod +x $HOME/pixelcade/system/update.sh

if [ "$auto_update" = true ] ; then #add git pull to startup
    if cat $HOME/pixelcade/system/pixelcade-startup.sh | grep -q 'sh ./update.sh'; then
       echo "${yellow}Auto-update was already added to pixelcade-startup.sh, skipping...${white}"
    else
      echo "${yellow}Adding auto-update to pixelcade-startup.sh...${white}"
      sudo sed -i '/^exit/i cd $HOME/pixelcade/system && sh ./update.sh' $HOME/pixelcade/system/pixelcade-startup.sh #insert this line before exit
    fi
fi

#if cat /boot/cmdline.txt | grep -q 'vt.global_cursor_default=0'; then
#   echo "${yellow}Blinking cursor already disabled, skipping...${white}"
#else
#  echo "${yellow}Disabling blinking cursor...${white}"
#  sudo sed -i "1 s|$| vt.global_cursor_default=0|" "/boot/cmdline.txt" #add this text at the end of the first line
#fi

if [ "$retropie" = true ] ; then
    # let's check if autostart.sh already has pixelcade added and if so, we don't want to add it twice
    #cd /opt/retropie/configs/all/
    if cat /opt/retropie/configs/all/autostart.sh | grep -q 'pixelcade'; then
      echo "${yellow}Pixelcade already added to autostart.sh, skipping...${white}"
    else
      echo "${yellow}Adding Pixelcade /opt/retropie/configs/all/autostart.sh...${white}"
      #sudo sed -i '/^emulationstation.*/i echo "Waiting for PixelcadeLCD to be ready..."' /opt/retropie/configs/all/autostart.sh
      sudo sed -i '/^emulationstation.*/i until $(curl --output /dev/null --silent --head --fail http://pixelcadedx.local:8080); do printf '\''|'\''; sleep 1; done' /opt/retropie/configs/all/autostart.sh
      sudo sed -i '/^emulationstation.*/i cd $HOME/pixelcade && ./pixelweb -b &' /opt/retropie/configs/all/autostart.sh #insert this line before emulationstation #auto
      #sudo sed -i '/^emulationstation.*/i sleep 10 && cd $HOME/pixelcade/system && ./pixelcade-startup.sh' /opt/retropie/configs/all/autostart.sh #insert this line before emulationstation #auto
      if [ "$attractmode" = true ] ; then
          echo "${yellow}Adding Pixelcade for Attract Mode to /opt/retropie/configs/all/autostart.sh...${white}"
          sudo sed -i '/^attract.*/i cd $HOME/pixelcade && ./pixelweb -b &' /opt/retropie/configs/all/autostart.sh #insert this line before attract #auto
          #sudo sed -i '/^attract.*/i cd $HOME/pixelcade && ./pixelweb -b &' /opt/retropie/configs/all/autostart.sh #insert this line before attract #auto
          sudo sed -i '/^attract.*/i sleep 10 && cd $HOME/pixelcade/system && ./pixelcade-startup.sh' /opt/retropie/configs/all/autostart.sh #insert this line before attract #auto
      fi
    fi
    echo "${yellow}Installing Fonts...${white}"
    cd $HOME/pixelcade
    mkdir $HOME/.fonts
    sudo cp $HOME/pixelcade/fonts/*.ttf /$HOME/.fonts
    sudo apt -y install font-manager
    sudo fc-cache -v -f
    sudo chmod +x /opt/retropie/configs/all/autostart.sh
else #there is no retropie so we need to add pixelcade /etc/rc.local instead
  echo "${yellow}Installing Fonts...${white}"
  cd $HOME/pixelcade
  mkdir $HOME/.fonts
  sudo cp $HOME/pixelcade/fonts/*.ttf /$HOME/.fonts
  sudo apt -y install font-manager
  sudo fc-cache -v -f
  echo "${yellow}Adding Pixelcade to Startup...${white}"
  cd $HOME/pixelcade/system
  sudo chmod +x $HOME/pixelcade/system/autostart.sh
  sudo cp pixelcade.service /etc/systemd/system/pixelcade.service
  #to do add check if the service is already running
  sudo systemctl start pixelcade.service
  sudo systemctl enable pixelcade.service
fi

#setting up udev rules to change /dev/pixelcade0
#cd $HOME/pixelcade
#sudo cp 50-pixelcade.rules /etc/udev/rules.d
#sudo /etc/init.d/udev restart

# let's send a test image and see if it displays
#sleep 5
#cd $HOME/pixelcade
#curl http://localhost/arcade/stream/mame/1941

#let's write the version so the next time the user can try and know if he/she needs to upgrade
echo $version > $HOME/pixelcade/pixelcade-version

# let's change the hostname from retropie to pixelcade and note that the dns name will be pixelcade.local
cd /etc
if cat hostname | grep -q 'pixelcade'; then
   echo "${yellow}Pixelcade already added to hostname, skipping...${white}"
else
   sudo sed -i 's/retropie/pixelcade/g' hostname
   sudo sed -i 's/raspberrypi/pixelcade/g' hostname
fi

if cat hosts | grep -q 'pixelcade'; then
   echo "${yellow}Pixelcade already added to hosts, skipping...${white}"
else
  sudo sed -i 's/retropie/pixelcade/g' hosts
  sudo sed -i 's/raspberrypi/pixelcade/g' hosts
fi

install_succesful=true

if [ "$install_succesful" = true ] ; then
  while true; do
      read -p "${magenta}You'll need to Reboot, ok to Reboot Now? (y/n)${white}" yn
      case $yn in
          [Yy]* ) sudo reboot; break;;
          [Nn]* ) echo "${yellow}Please reboot when you get a chance" && exit;;
          * ) echo "Please answer yes or no.";;
      esac
  done
fi
