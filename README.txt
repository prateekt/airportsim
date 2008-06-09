FAAv2 Delivery README
-----------------------

Dear Grader,viewer, or Curious User:

Here is the delivery for FAA v2. It sports the following features:

-Ability to log in as an Air Traffic Controller (Ground Controller, Local Controller, or Clearance Delivery)
and participate in the hectic day-to-day activities of an ATC at a busy international airport.
	
	*You will be able to give commands and interact with Pilots that are landing and departing.
	*It is your job to ensure that planes do not run into each other.
	*The pilots are not real - they're simulations, so this is actually training software.
	
	
-Text-to-speech for all the commands that are passed between the pilots and ATCs.

-For the ATC's that you are not logged in as, will act autonomously so you will feel like you're
	interacting in a real-time environment at an airport.
	
-The autonomous ATC agents act intelligently - they use A* path-finding to determine the best
	route for a pilot to take when arriving/departing.
	
-The UI for the ATC agents is intuitive and easy to use. It features a neat waypointing system
	to give directions to planes.
	

=====================================

The full manual for running the FAA Control Tower Training System can be found in the folder /docs, under
FAA Control Operating Manual.pdf.

=====================================

To run the system, simply run the "ant" commands:

ant clean
ant compile
ant run.gui

======================================

To test the system, simply run the test suite via ant:

ant clean
ant compile
ant test

Be patient, the tests might take a while.


