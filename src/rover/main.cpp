#include "camera.hpp"
#include <stddef.h>
#include <iostream>

int main() {
	// Open all cameras
	camera::CaptureSession session1;
	camera::CaptureSession session2;
	camera::CaptureSession session3;
	camera::CaptureSession session4;

	std::cout << "Hello Wowld:" << std::endl;

	/* TODO:Consider making each instance its own object */
	camera::Error err1 = camera::open(&session1, "/dev/video0", 1280, 720);
	//std::cout << "1 " << camera::get_error_string(err1) << std::endl;
	err1 = camera::start(&session1);
	//std::cout << camera::get_error_string(err1) << std::endl;

	camera::Error err2 = camera::open(&session2, "/dev/video0", 1280, 720);
	err2 = camera::start(&session2);
	//std::cout << camera::get_error_string(err2) << std::endl;

	camera::Error err3 = camera::open(&session3, "/dev/video0", 1280, 720);
	err3 = camera::start(&session3);
	//std::cout << camera::get_error_string(err3) << std::endl;

	camera::Error err4 = camera::open(&session4, "/dev/video0", 1280, 720);
	err4 = camera::start(&session4);
	//std::cout << camera::get_error_string(err4) << std::endl;

	while(true) {
		//Grab a frame
		uint8_t* frame_buffer;
		std::size_t frame_size;

		err1 = camera::grab_frame(&session1, &frame_buffer, &frame_size);
		//std::cout << "1" << camera::get_error_string(err1) << std::endl;
		err1 = camera::return_buffer(&session1, frame_buffer);
		//std::cout << "2" << camera::get_error_string(err1) << std::endl;

	}

	// Close the session at the end
	camera::close(&session1);
	camera::close(&session2);
	camera::close(&session3);
	camera::close(&session4);
	return 0;
}