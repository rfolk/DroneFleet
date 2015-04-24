# DroneFleet

# Setting up a drone for environment:

Assumption is drones are brand-new:

1. Download source from: https://sites.google.com/site/androflight/open-source
2. Extract source
3. cd sourceFile/src/data/ (Keep this directory open during the rest of these instructions)
4. Open install.sh (Change to the following)

  a. ESSID=AndroidAP  
  b. IP=192.168.43.10  
  c. NETMASK=255.255.255.0   

5. Save install.sh
6. Turn on Drone
7. Connect to Drone Access Point with a wireless-capable computer.

```
> denotes a command in a terminal or telnet capable program. 
> telnet 192.168.1.1    (No password required)
> vi install.sh
Copy and paste the install.sh on our local machine to vi
> vi ARAutoConnect.sh
Copy and paste the ARAutoConnect.sh on our local machine to vi
> vi uninstall.sh
Copy and paste the uninstall.sh
> chmod 0755 install.sh
> chmod 0755 ARAut	oConnect.sh
> chmod 0755 uninstall.sh
> ./install.sh
> vi /data/config.ini
Set “wifi_mode = 1”
> reboot (Disconnection should occur and will then join ESSID once finished)
```

# Simulator Installation(Ubuntu 13.10 ~ Saucy):

> All of these steps are done within a Ubuntu virtual machine.
> Virtualization software used is VMware Fusion 6 for Mac.

1. Compatible with ROS Indigo   
2. Compatible with Gazibo 2.2   
3. Compatible with tum_simulator ( AR.Drone 2 simulation project )  

## ROS Indigo Installation:

1. sudo sh -c 'echo "deb http://packages.ros.org/ros/ubuntu saucy main" > /etc/apt/sources.list.d/ros-latest.list'
2. wget https://raw.githubusercontent.com/ros/rosdistro/master/ros.key -O - | sudo apt-key add -
3. sudo apt-get update
4. sudo apt-get install ros-indigo-desktop-full
5. sudo reboot
6. apt-cache search ros-indigo
7. sudo rosdep init
8. rosdep update
9. echo "source /opt/ros/indigo/setup.bash" >> ~/.bashrc
10. source ~/.bashrc
11. source /opt/ros/indigo/setup.bash
12. sudo apt-get install python-rosinstall
13. Gazebo 2.2 Installation:
14. sudo sh -c 'echo "deb http://packages.osrfoundation.org/gazebo/ubuntu saucy main" > /etc/apt/sources.list.d/gazebo-latest.list'
15. wget http://packages.osrfoundation.org/gazebo.key -O - | sudo apt-key add -
16. sudo apt-get update
17. sudo apt-get install gazebo2
18. echo '/usr/local/lib' | sudo tee /etc/ld.so.conf.d/gazebo.conf 
19. sudo ldconfig

## ROS Indigo / Gazebo 2.2 Integration Package:

1. sudo apt-get install ros-indigo-gazebo-ros-pkgs ros-indigo-gazebo-ros-control
2. tum_simulator ( for ROS Indigo ):
3. mkdir -p ~/tum_simulator_ws/src
4. cd  ~/tum_simulator_ws/src
5. catkin_init_workspace
6. git clone https://github.com/AutonomyLab/ardrone_autonomy.git
7. git clone https://github.com/occomco/tum_simulator.git
8. cd ..
9. rosdep install --from-paths src --ignore-src --rosdistro indigo -y
10. catkin_make
11. source devel/setup.bash (needs to be appended to ~/.bashrc
12. roslaunch cvg_sim_gazebo ardrone_testworld.launch

## Black-screen fix: 

1. sudo apt-get purge nvidia*
2. sudo apt-get install --reinstall xserver-xorg-video-intel  libgl1-mesa-glx libgl1-mesa-dri xserver-xorg-core
3. sudo dpkg-reconfigure xserver-xorg
4. sudo update-alternatives --remove gl_conf /usr/lib/nvidia-current/ld.so.conf
5. sudo reboot
6. sudo apt-add-repository ppa:xorg-edgers/ppa
7. This provides the necessary repository (assuming you removed it)
8. sudo apt-get update
9. sudo apt-get install bumblebee-nvidia nvidia-319 nvidia-settings-319
10. sudo reboot (Will take a little longer to reboot).

# References for Project Instructions:

## Black Screen Reference:

1. http://askubuntu.com/questions/389901/how-do-i-get-opengl-working-on-an-nvidia-geforce-gt-750m

## API Resource:

1. https://projects.ardrone.org/attachments/download/365/ARDrone_SDK_1_7_Developer_Guide.pdf
2. https://abstract.cs.washington.edu/~shwetak/classes/ee472/assignments/lab4/drone_api.pdf
3. http://svn.mikrokopter.de/mikrosvn/Projects/C-OSD/arducam-osd/libraries/GCS_MAVLink/incude/mavlink/v0.9/common/mavlink_msg_global_position_int.h
4. https://projects.ardrone.org/boards/1/topics/show/5924
 
## API GPS:

1. http://wiki.paparazziuav.org/wiki/AR_Drone_2/GPS
2. https://github.com/felixge/node-ar-drone/issues/74
3. https://github.com/felixge/node-ar-drone
4. https://github.com/andrew/ar-drone-gps
5. https://pixhawk.ethz.ch/mavlink/
6. http://en.wikipedia.org/wiki/MAVLink
7. http://qgroundcontrol.org/mavlink/start
8. http://qgroundcontrol.org/mavlink/waypoint_protocol#write_mav_waypoint_list

## Scripts:

1. http://jsfiddle.net/9cscm54o/3/ (Hex String to Java byte[])
2. http://jsfiddle.net/yeuLzatt/ (Algorithm Results)
3. http://itouchmap.com/latlong.html (Coordinate tool, used console in Chrome )

## Using “tum_simulator” for ROS :

1. http://wiki.ros.org/tum_simulator (Original Project)
2. https://github.com/occomco/tum_simulator (Ros/Gazebo: Indigo build)

## SDK for Drone 2.0:

1. https://projects.ardrone.org/ 

## Basic Movements:

1. http://www.robotappstore.com/Knowledge-Base/How-to-Control-ARDrone-Movements-During-Flight/99.html
2. https://projects.ardrone.org/boards/1/topics/show/852
3. https://projects.ardrone.org/boards/1/topics/show/3665

