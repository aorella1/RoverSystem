#include <std_msgs/String.h>
#include "ros/ros.h"
#include "geometry_msgs/Twist.h"

#include <stdio.h>
#include <string>
#include <iostream>
#include <cstdio>
#include <unistd.h>
#include "serial/serial.h"

using namespace std;

serial::Serial my_serial("/dev/ttyACM0", 9600, serial::Timeout::simpleTimeout(1000));

void moveCallback(const geometry_msgs::Twist& msg){

    unsigned char dataFF[2], dataFE[2], dataFD[2], dataFC[2], data02[2], data03[2];
    dataFF[0] = 0xFF; dataFF[1]=0;
    dataFE[0] = 0xFE; dataFE[1]=0;
    dataFD[0] = 0xFD; dataFD[1]=0;
    dataFC[0] = 0xFC; dataFC[1]=0;
    data02[0] = 0x02; data02[1]=0;
    data03[0] = 0x03; data03[1]=0;

    printf("msg.linear.y: %f msg.angular.z: %f \n", msg.linear.y, msg.angular.z);

    int test;

    test = (int)(msg.linear.y*32.0) + (msg.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test;
    printf("left motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);
    test = (int)(msg.linear.y*32.0) - (msg.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test+1;
    printf("right motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);
}

int main(int argc, char **argv) {

    /*----SETUP BEGIN----*/
    /*-ROS node setup-*/
    ros::init(argc, argv, "rover_tester"); //init node
    ros::NodeHandle n; //node handler
    ros::Subscriber sub = n.subscribe("move", 1000, moveCallback);

    printf("Node started!");

    while(ros::ok()){
        ros::spinOnce();
    }

    //ros::spin();

    return 0;
}

