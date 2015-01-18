This folder contains:
1. pay-date-calculator: the eclipse project folder
2. paydatecalculator.jar: the executable jar
3. paydatecalculator.bat a bat which can also be used to run the jar.

@Requirements
To build the project: 
 - eclipse (Luna)
 - maven
 - JDK 1.7.0_67 (Tested and compiled against)

 To run the jar:
 - JRE 1.7
 
@Usage: 
<path to java> -jar paydatecalculator.jar [-b <arg>] [-f <arg>] [-h] [-s <arg>]
 -b <arg>    The day in which the bonus needs to be paid out (Ex. for the
             24th this should be 24)
 -f <arg>    The name of the file in which the dates will be saved
 -h,--help   print this message
 -s <arg>    The day in which the salary needs to be paid out (Ex. for
             the 24th this should be 24) 

The .bat file:
The bat file relies on "java" being set up in the environment variables to point to the java.exe executable.

After running the executable the results will pe printed in the selected file (or the default one if no file was specified) and to the standard out (console).		 