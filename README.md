EventViewerREST
===============

EventViewerREST consist of back-end RESTFul web-services that have been written entirely by me (Pathum Mudannayake) to retrieve meta-data and then results for custom queries that can be constructed using that meta-data. The data-store is a multi-dimensional data-mart and an OLAP server (Mondrian) works on top of it to facilitate mulidimensional queries (MDX). The data-mart and the OLAP schema was fully designed and developed by me. A reference full-stack implementation is available at: http://eventviewer.asap.um.maine.edu/ev2014/eventviewer/. I have involved in the front-end development through sharing my ideas and concepts      (however, development of the front-end has been done by a different developer).

EventViewer is a scientific multi-dimensional big-data visualization tool. The project has spun off as a proto-type implementation of a concept envisioned by Dr. Kate Beared (Spatial Information Science and Engineering, School of Computing and Information Sciences, Unviersity of Maine) as presented through one of her research articles. The idea is to support multi-dimensional scientific event data visualization, which has not been addressed thoroughly so far.

An event is some incident that has a scientific importance. For example, crime events would consist of a time of the crime, location (Zip code or the geo-location) of the crime and the type of the crime (theft, assault etc.). Each of these dimensions are hierarchical in nature. That is, a single time stamp has a day, month and a year (many other categorizations such as season, quarter etc. are also possible), a location would has an area, region and some more hierarchical categories. Similarly, a theme could be further classified into different simantically related sub-categories. There can be a multitude of event types. They can be oceanographic, geological, celestial or sociological to name a few. All such events have three dimensions: temporal, spatial and thematic.

Services
---------

Services are two-fold: Meta-data and Query-data

The meta-data service is called first and fore-most by any API user. Meta-data service provides all the information that is relavent for building custom queries. For example, for crime data, the types of crimes, times of crimes (years, months and dates) and locations of crimes that are querible and available in the data-store is supplied to the user.

A user creates a query using the meta-data and call the Query-data API. Query-data API uses that data to filter, dice and slice the data in the data-store to provide the results back to the user.

Architectural Decisions
-----------------------

The data-stores is based on OLAP rather than OLTP. This is mainly because the application is geared towards heavy data mining and retrieving rather than storing. Therefore, a non-normalized star-schema is used to organize the three dimensions: time, location and theme around the facts such as the magnitudes or intensities of the events.

Postgres is used as the primary DBMS. This is owing to the fact that Mondrian OLAP server (one of the OLAP servers that are available in the Free and OpenSource domain) is geared to work with Postgres without causing major configuration difficulties and the two technolgies have a track record of working together very well. Postgres DBMS is spatially enabled (with PostGIS) to facilitate a future spatial data incorporation.

The API consist of RESTFul services so that in the future (as envisioned) many developers can seamlessly connect to the services and make use of the sets of data that is going to be availble. Also, it is expected to provide support to multiple platforms (eg. Mobile).

Java is used as the programming environment. The experties of the developers (especially me) aligned well with using Java. Also, the tremendous support that Java has for implementing RESTFul services, working with json (through libraries such as gson by google) and the convenience of collaborating with other developers (if anyother developer needs to work in the back-end) were the other factors for choosing java.

Code and Comments
------------------

Detaild comments (as applicable) are provided in the code.

Testing the Services
---------------------

A call to http://eventviewer.asap.um.maine.edu/ev2014/eventviewer/ will present the front-end of the application supported by the EventViewerREST meta-data service. A user who is unfamiliar with the environment can click on the bottom "Query" button to request sample data and visualize it in the front-end platform withuot creating any query (by dragging and dropping elements from different dimensions).

Following is a sample query that can be used to test the query API (in JS):

var timeArray = ["[TimeFilter].[2001]","[TimeFilter].[2002]","[TimeFilter].[2003]"]; 

var locArray = ["[Gulf of Maine].[Coastal].[Western Maine Shelf].[B].[1-20]"]; var themeArray = ["[Ocean].[Sigma-Tdensity].[Threshold].[Mixed125]"];

var paramArray = [locArray,timeArray,themeArray];

url: "http://eventviewer.asap.um.maine.edu:8080/evolapservice/rest/evquery?filter="+JSON.stringify(paramArray)

Additional Notes
-----------------

This project is still under constant improvement. Therefore, there are places in the code that I am continously changing to make the services run faster and the code look more readable and elegent.

The data that is accessible through the publicly hosted version of the project is only a sample set of the actual data-set that is hosted with minimum security.


