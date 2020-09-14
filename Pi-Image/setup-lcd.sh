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
version=5  #increment this as the script is updated
#echo "${red}red text ${green}green text${reset}"

cat << "EOF"
       _          _               _
 _ __ (_)_  _____| | ___ __ _  __| | ___
| '_ \| \ \/ / _ \ |/ __/ _` |/ _` |/ _ \
| |_) | |>  <  __/ | (_| (_| | (_| |  __/
| .__/|_/_/\_\___|_|\___\__,_|\__,_|\___|
|_|
EOF

echo "${magenta}       Pixelcade for RetroPie Installer ${white}"
echo ""
echo "${red}IMPORTANT:${white} This installer will work on a Pi 2, Pi Zero W, Pi 3B, Pi 3B+, and Pi 4"
echo "Now connect Pixelcade to a free USB port on your Pi (directly connected to your Pi or use a powered USB hub)"
echo "Please ensure the toggle switch on the Pixelcade board is pointing towards USB and not BT"

# let's check the version and only proceed if the user has an older version
if [[ -d "$HOME/pixelcade" ]]; then
            while true; do
                read -p "${magenta}Existing Pixelcade installation detected. This installer will DELETE your current Pixelcade installation at /home/pi/pixelcade and re-install a new version with the latest artwork. Do you want to continue? (y/n) ${white}" yn
                case $yn in
                    [Yy]* ) sudo rm -r /home/pi/pixelcade ; break;;
                    [Nn]* ) echo "${yellow}Exiting..." && exit;;
                    * ) echo "Please answer y or n";;
                esac
            done
fi

#now let's download the second setup script
cd ~
curl -LO pixelcade.org/pi/setup-lcd2.sh
sudo chmod +xs setup-lcd2.sh

#let's add setup-lcd2.sh to autostart
sudo sed -i '/^emulationstation.*/i ~/setup-lcd2.sh' /opt/retropie/configs/all/autostart.sh
#now let's comment out emulationstation
sudo sed -i "/^emulationstation.*/c\#emulationstation #auto" /opt/retropie/configs/all/autostart.sh

while true; do
      read -p "${magenta}Your arcade cabinet will now reboot and you'll finish the remaining Pixelcade installation in front of your arcade cabinet using your arcade controls & buttons. OK to Reboot Now? (y/n)${white}" yn
      case $yn in
          [Yy]* ) sudo reboot; break;;
          [Nn]* ) echo "${yellow}Please reboot when you get a chance" && exit;;
          * ) echo "Please answer yes or no.";;
      esac
done
