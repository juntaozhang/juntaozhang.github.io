SOURCES = $(wildcard *.c)
OBJS=$(patsubst %.c,%.o,$(patsubst %.cc,%.o,$(SOURCES)))
CC=gcc
CFLAGS=-Wall -O -g
TARGET = ./hello
#hello:$(OBJS)
#	$(CC) $(OBJS) -o hello
#test_include1.o:test_include1.c test_include1.h
#	$(CC) $(CFLAGS) -c test_include1.c -o test_include1.o
#test_include.o:test_include.c test_include1.h
#	$(CC) $(CFLAGS) -c test_include.c -o test_include.o
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@
$(TARGET) : $(OBJS)
	$(CC) $(OBJS) -o $(TARGET)
clean:
	rm *.o hello -rf