call mvn clean compile assembly:single
@copy C:\Users\imodm\eclipse-workspace\core-development\target\ScorchCore.jar C:\Users\imodm\Documents\Servers\TestServer\plugins /y 
@copy C:\Users\imodm\eclipse-workspace\core-development\target\ScorchCore.jar C:\Users\imodm\Documents\Servers\DummyServer\plugins /y 
echo Successfully built and copied
timeout 5