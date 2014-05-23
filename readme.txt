A simple spring bean that enables Saiku to load requests on startup and populate Mondrian cache

Compilation :
=============

mvn as usual


Deployment and configuration :
==============================

Copy created jar into WEB-INF/lib directory of Saiku webapp

Open  WEB-INF/lib/saiku-bean.xml and add following beans declarations

	<!-- create another olapQueryBean that is not a 'session scoped' bean
	(change the default behavior of initial olapQueryBean is a bad idea) 
	 -->
	<bean id="olapQueryBean2" class="org.saiku.service.olap.OlapQueryService">
		<property name="olapDiscoverService" ref="olapDiscoverServiceBean" />
	</bean>
	
	<!-- Yet another clone of repositoryBean so that we can specify a different
	path for saved queries to load at startup   -->
	<bean id="repositoryBean2" 
		class="org.saiku.web.rest.resources.BasicRepositoryResource">
		<property name="olapQueryService" ref="olapQueryBean2" />
		<property name="path" value="res:saiku-repository" />
		<!-- <property name="path" value="/tmp"/> -->
	</bean>
	
	<bean id="mondrianCachePreloader" class="fr.saucedallas.MondrianCachePreloader">
		<property name="olapQueryService" ref="olapQueryBean2" />
		<property name="basicRepositoryResource" ref="repositoryBean2" />
 	</bean>


Now save requests via Saiku interface, these requests will be reloaded at next startup.


For repositoryBean2, 'path' property indicates where to lookup requests to load on startup.
Here it points in same repository as requests saved via Saiku user interface, which is WEB-INF/classes/saiku-repository.
You see that this folder contains saved queries, as .saiku files.
But you can specify another path, e.g  an absolute path on your os, and put saiku files on it. 
Or a just a sub folder of WEB-INF/classes/saiku-repository. 


configure logs :
================ 

Add this appender in WEB-INF/classes/log4j.xml :

   <category name="fr.saucedallas">
     <priority value="DEBUG"/>
     <appender-ref ref="SAIKUCONSOLE"/>
   </category>

Requests preload will be logged in tomcat-dir/logs/saiku.log


Enjoy other Saiku tips at http://sauce-dallas.blogspot.fr