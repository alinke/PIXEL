while pgrep omxplayer >/dev/null; do sleep 1; done
(sleep 10; mpg123 -Z /home/pi/RetroPie/roms/music/*.mp3 >/dev/null 2>&1) &
echo "Searching for PixelcadeLCD (will timeout if not found)"; lcd_marquee=true; i=0; until $(curl --output /dev/null --silent --head --fail http://pixelcadedx.local:8080); do
printf "|" ;sleep 1;((i=i+1))
if [ $i -gt 2 ]; then  #will be 6 seconds in between ticks if pixelcadelcd not plugged in and only 1s if pixelcadelcd is plugged in
   echo -e " \n[ERROR] Could not find PixelcadeLCD"; lcd_marquee=false; break; fi
done
if [ "$lcd_marquee" = true ] ; then
   echo -e "\n[FOUND] PixelcadeLCD"
   startup=true
else
   sudo fbi ~/pixelcade/system/lcd1024alert.png --noverbose -T 1 -d /dev/fb0 &
   ((start=`date +%s`))
   read -rsn 1 -t 5 -p "Waiting for user to quit or continue…";echo
   ((end=`date +%s`))
   ((total = end - start))
   if [ $total -gt 4 ]; then
      startup=true
      #echo -e " \nStarting up"
      sudo killall fbi
   else
      startup=false
   fi
fi
if [ "$startup" = false ] ; then
   #echo -e "\nAborting start ip at user request."
   sudo killall fbi
fi
cd $HOME/pixelcade && ./pixelweb -b &
emulationstation #auto
