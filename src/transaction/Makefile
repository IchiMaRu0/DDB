RMIREGPORT =3345 
PROJECTROOT = ..
# PATH = /usr/bin
all : server client

server : ResourceManager.class \
         ResourceManagerImpl.class \
         TransactionManager.class \
         TransactionManagerImpl.class \
         WorkflowController.class \
         WorkflowControllerImpl.class \
         InvalidTransactionException.class \
         TransactionAbortedException.class \
         RMManagerCars.class \
         RMManagerCustomers.class \
         RMManagerFlights.class \
         RMManagerHotels.class \

client: Client.class

runregistry :
	rmiregistry -J-classpath -J$(PROJECTROOT) $(RMIREGPORT)

runtm : server
	java -classpath $(PROJECTROOT)  -Djava.security.policy=./security-policy transaction.TransactionManagerImpl

runrmflights : server
	java -classpath $(PROJECTROOT)   -Djava.security.policy=./security-policy transaction.RMManagerFlights

runrmrooms : server
	java -classpath $(PROJECTROOT)  -Djava.security.policy=./security-policy transaction.RMManagerHotels

runrmcars : server
	java -classpath $(PROJECTROOT)   -Djava.security.policy=./security-policy transaction.RMManagerCars

runrmcustomers : server
	java -classpath $(PROJECTROOT)   -Djava.security.policy=./security-policy transaction.RMManagerCustomers

runwc : server
	java -classpath $(PROJECTROOT)  -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl

runclient : client
	java -classpath $(PROJECTROOT)  transaction.Client

clean :
	$(RM) *.class
	$(RM) ./entity/*.class

%.class : %.java
	javac -classpath $(PROJECTROOT) $<

#%_Stub.class : %.java
#	rmic -classpath $(PROJECTROOT) -d $(PROJECTROOT) transaction.$*


.PHONY : runregistry runtm runrmflights runrmrooms runrmcars runrmcustomers runwc runclient clean
