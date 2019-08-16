JARS :=

JAVAC	:= javac

DEPENDS := $(wildcard src/*.java)

.PHONY: all game run clean zip

all:	

bin:
	mkdir bin

bin/Game.class : $(DEPENDS) bin
	$(JAVAC) -cp .:$(JARS) -d bin $(DEPENDS)

run : bin/Game.class
	java -cp .:./bin Game

clean:
	rm -f src/*.class bin/* test/*.class
	rm -rf *.zip
