SOURCES = main.cpp
HEADERS = 

LIBRARIES = libavcodec libavutil opencv

avsender: $(SOURCES) $(HEADERS)
	g++ -std=c++11 -Wall -g -o $@ $(SOURCES) `pkg-config --cflags --libs $(LIBRARIES)`