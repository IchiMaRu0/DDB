PROJECTROOT = ..

clean :
	rm -rf ./data/*
	rm -rf ./results/*
	$(RM) *.class

all :
	javac -classpath $(PROJECTROOT) *.java

test:
#Basic Test:


#ACID Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=Atomicity test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=Consistency test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=Isolation test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=Durability test.LaunchTest

#Function Test


#Die Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieRMAfterEnlist test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieRMAfterPrepare test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieRMBeforeAbort test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieRMBeforeCommit test.LaunchTest

	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieTMBeforeCommit test.LaunchTest
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieTMAfterCommit test.LaunchTest

	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DieWC test.LaunchTest


#Lock Test:
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DtestName=DeadLock test.LaunchTest

demo:
	java -classpath $(PROJECTROOT)  -DtestName=QueryforBill test.LaunchTest







