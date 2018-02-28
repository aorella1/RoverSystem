# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.5

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/IEEE_PI/ros_workspace/src

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build

# Include any dependencies generated for this target.
include serial/tests/CMakeFiles/serial-test-timer.dir/depend.make

# Include the progress variables for this target.
include serial/tests/CMakeFiles/serial-test-timer.dir/progress.make

# Include the compile flags for this target's objects.
include serial/tests/CMakeFiles/serial-test-timer.dir/flags.make

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o: serial/tests/CMakeFiles/serial-test-timer.dir/flags.make
serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o: /home/IEEE_PI/ros_workspace/src/serial/tests/unit/unix_timer_tests.cc
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o"
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests && /usr/bin/c++   $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o -c /home/IEEE_PI/ros_workspace/src/serial/tests/unit/unix_timer_tests.cc

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.i"
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests && /usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/IEEE_PI/ros_workspace/src/serial/tests/unit/unix_timer_tests.cc > CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.i

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.s"
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests && /usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/IEEE_PI/ros_workspace/src/serial/tests/unit/unix_timer_tests.cc -o CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.s

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.requires:

.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.requires

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.provides: serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.requires
	$(MAKE) -f serial/tests/CMakeFiles/serial-test-timer.dir/build.make serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.provides.build
.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.provides

serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.provides.build: serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o


# Object files for target serial-test-timer
serial__test__timer_OBJECTS = \
"CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o"

# External object files for target serial-test-timer
serial__test__timer_EXTERNAL_OBJECTS =

/home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer: serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o
/home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer: serial/tests/CMakeFiles/serial-test-timer.dir/build.make
/home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer: gtest/libgtest.so
/home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer: /home/IEEE_PI/ros_workspace/devel/lib/libserial.so
/home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer: serial/tests/CMakeFiles/serial-test-timer.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable /home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer"
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/serial-test-timer.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
serial/tests/CMakeFiles/serial-test-timer.dir/build: /home/IEEE_PI/ros_workspace/devel/lib/serial/serial-test-timer

.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/build

serial/tests/CMakeFiles/serial-test-timer.dir/requires: serial/tests/CMakeFiles/serial-test-timer.dir/unit/unix_timer_tests.cc.o.requires

.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/requires

serial/tests/CMakeFiles/serial-test-timer.dir/clean:
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests && $(CMAKE_COMMAND) -P CMakeFiles/serial-test-timer.dir/cmake_clean.cmake
.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/clean

serial/tests/CMakeFiles/serial-test-timer.dir/depend:
	cd /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/IEEE_PI/ros_workspace/src /home/IEEE_PI/ros_workspace/src/serial/tests /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests /home/IEEE_PI/BinghamtonRover2017/scratch/CPP_ROVER/ros_workspace/build/serial/tests/CMakeFiles/serial-test-timer.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : serial/tests/CMakeFiles/serial-test-timer.dir/depend

