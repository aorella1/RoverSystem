SOURCES = main.cpp camera.cpp
HEADERS = camera.h

sender: $(SOURCES) $(HEADERS)
	g++ -std=c++11 -Wall -g -o $@ $(SOURCES) `pkg-config --cflags --libs opencv`