# To begin
-This is the submodule in mobitopp parent repository. You need to clone this project after you load the mobitopp parent project into IDE. The internal links should already be defined in the **build.gradle** and the **settings.gradle** file. You need to execute the command from mobitopp-parent/application in the Gradle shortcut.

-There is no .net file as they are too large. You need to prepare for it from PTV Visum (preferably). You need to specify the file location in all the **.yaml** files. My default location for this file is in data_clayton/clayton.

-You can couple MATSim with or without the accessibility computation. Simply change the matsimConfig in **simulation-matsim.yaml**. There is a equil config file for MATSim simulation only.

-The current accessibility computation is from predefined facilities. You need to specify the radius you want to compute for the accessibility. This is defined in facilities in **simulation-matsim.yaml**.
