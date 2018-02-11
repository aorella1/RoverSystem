SOURCES = main.cpp

LIBRARIES = libavformat libavcodec libswscale libswresample libavutil opencv

libavtest: $(SOURCES)
	g++ -std=c++11 -Wall -g -o $@ $(SOURCES) `pkg-config --cflags --libs $(LIBRARIES)`