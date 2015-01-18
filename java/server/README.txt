@Requirements
JDK 1.7.0_67 (Tested and compiled against)

The server starts considering the root directory the directory in which it is located. To be able to properly start the server
requires the server.properties file. Without this file the server will not work.

@Demo
The server_demo project contains a demo server together with a few demo files.
To launch the server you can either invoke it from the command line using java -jar homework-1.0-jar-with-dependencies.jar or 
if running on Windows you can use the start_server.bat file. By default the server will start on port 8080 (http://http://localhost:8080/)

@Description
The server uses annotations to register dynamic (code based) handlers which can respond to request coming over HTTP. The handlers are context-locked 
i.e. they can only handle predefined requests.
There is a File Manager which can serve up static content.

@TODO
Improve logging.
Add more comments.
Remove a File hack.
Add more tests.


