SOURCES = main.cpp
HEADERS =

receiver: $(SOURCES) $(HEADERS)
	g++ -std=c++11 -Wall -g -o $@ $(SOURCES) `pkg-config --cflags --libs opencv`