SOURCES = main.cpp

LIBRARIES = ogg theoraenc theoradec opencv

theorareceiver: $(SOURCES)
	g++ -std=c++11 -Wall -g -o $@ $(SOURCES) `pkg-config --cflags --libs $(LIBRARIES)`