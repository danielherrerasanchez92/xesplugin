#XESPlugin
This is a plugin for Pentaho Data Integration to create XES files for Process Mining. Was developed by Daniel Herrera Sanchez.

###[Alpha release](https://github.com/syn920123/xesplugin/releases/tag/v0.0.1-alpha)
###[Source code](https://github.com/syn920123/xesplugin)

The following elements are required to use XESPlugin:  

    Pentaho 	Data Integration, which can be downloaded from: 	(http://community.pentaho.com/projects/data-integration/) 
    XESPlugin 	and OpenXES libraries (within 	the compressed files) 

The procedure to integrate XESPlugin into Pentaho DI is very simple. You just need to copy the XESPlugin folder containing the .jar into the Pentaho plugins folder, then copy the OpenXES libraries into Pentaho's libs folder. Once this is done, the plugin can be accessed from the Output section in Pentaho DI.  
![output](http://i.imgur.com/Uk2T2Vr.png)

Once the XESPlugin configuration window is opened, two fields are visible at the top. The first field allows the user to modify the name of the step, which it is displayed on the Pentaho DI transformations board. The second field is the output path of the resulting XES file, followed by a search button. Pressing the button opens a window to browse among PC folders so you can indicate the address where you want to store the generated XES. This address, once selected, is displayed in the text field next to the button, allowing further modifications to the route. If no path is selected to store the plugin, it generates the XES file in the Pentaho DI base folder. Right now XESPlugin is divided into three tabs, each one contains the elements to build a XES log. The distribution of the elements by tabs is presented below:

Process Tab:   
![process tab](http://i.imgur.com/uHCmjbS.jpg)

The process tab contains the visual elements to relate the plugin with the event log in terms of Process Instance (Case), Activity and Lifecycle. Each combo box displays information about columns from the data flow. The Case and Activity fields are mandatory elements to fill in order for the plugin to work.  

Timestamp tab:  
![timestamp tab](http://i.imgur.com/TYLawiI.jpg)

The timestamp tab contains the visual elements to relate the plugin with the event log in terms of the timestamp and its format. If the format does not match any of the suggested formats listed, it is possible to write a new one following the lexical convention presented in the table below. The timestamp and format options can be left blank, however, if the timestamp is selected them it is mandatory to specify its format.           

| Day |  Month  | Year  | Hour  | Minute  | Second |  
|-----|---------|-------|-------|---------|--------|
|  dd |    MM   |  yyyy |  hh   |   mm    |   ss   |  
 
A valid example accepted by the application would be dd/MM/yyyy hh:mm:s. XESPlugin uses the Java classes SimpleDateFormat and Date to interpret dates and add those dates to the generated XES files. More information about creating dates expressions can be found at here. (https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) 

Resources tab:    
![res tab](http://i.imgur.com/pTUU38d.jpg)

The resources tab contains the visual elements to relate the plugin with the event log in terms of Resource, Role and Group. Like the other elements we’ve discussed so far, if any of these is not present in the data source, the combo box can be left blank and will not be taken into account by XESPlugin when generating the XES log. This plugin is at an early development stage as you may notice, so it’s far away from being fully implemented. So far its capable of recognizing an input stream of events, and parse a XES file using some of the standard extensions. It also allows you to store the configuration parameters in a *.ktr or Pentaho repository. This version is only intended to be tested by the community, hoping you can give us your thoughts about it. We will be working on improving the plugin based on what you can tell us, and also with some other interesting ideas we have on the plate right now.   
