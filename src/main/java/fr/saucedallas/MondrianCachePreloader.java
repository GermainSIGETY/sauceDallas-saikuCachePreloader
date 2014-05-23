package fr.saucedallas;

import java.util.List;
import java.util.UUID;

import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.dto.SaikuQuery;
import org.saiku.olap.query.QueryDeserializer;
import org.saiku.service.olap.OlapQueryService;
import org.saiku.web.rest.objects.SavedQuery;
import org.saiku.web.rest.resources.BasicRepositoryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author gesig
 * 
 */
@Component
public class MondrianCachePreloader implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory
			.getLogger(MondrianCachePreloader.class);

	private OlapQueryService olapQueryService;

	private QueryDeserializer queryDeserializer;

	private BasicRepositoryResource basicRepositoryResource;

	@Autowired
	public void setBasicRepositoryResource(
			BasicRepositoryResource basicRepositoryResource) {
		this.basicRepositoryResource = basicRepositoryResource;
	}

	@Autowired
	public void setOlapQueryService(OlapQueryService olapqs) {
		olapQueryService = olapqs;
	}

	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Preload saved queries...");
		preloadQueries();
		log.info("End of preloading saved queries...");

	}

	private void preloadQueries() {
		queryDeserializer = new QueryDeserializer();
		List<SavedQuery> queries = basicRepositoryResource.getSavedQueries();
		for (SavedQuery query : queries) {
			try {
				preloadQuery(query);
			} catch (Exception e) {
				log.error("error preloading query : " + e.getMessage());
			}
		}
	}

	private void preloadQuery(SavedQuery query) throws Exception {
		SaikuCube cube = queryDeserializer.getFakeCube(query.getXml());
		String queryName = UUID.randomUUID().toString();
		SaikuQuery sQuery = olapQueryService.createNewOlapQuery(queryName,query.getXml());
		log.info("execute a query, catalog : "+ cube.getCatalogName()+", query :"+ sQuery.getUniqueName());
		if(log.isDebugEnabled()) {
			log.debug("MDX is ", sQuery.getMdx());
		}
		olapQueryService.execute(queryName);
	}
}
