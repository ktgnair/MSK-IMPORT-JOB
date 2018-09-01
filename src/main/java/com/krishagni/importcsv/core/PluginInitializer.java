package com.krishagni.importcsv.core;

import org.springframework.beans.factory.InitializingBean;

import com.krishagni.catissueplus.core.importer.services.ObjectImporterFactory;
import com.krishagni.catissueplus.core.importer.services.ObjectSchemaFactory;

public class PluginInitializer implements InitializingBean {
	
	private ObjectSchemaFactory objectSchemaFactory;
	
	private ObjectImporterFactory objectImporterFactory;
	
	public void setObjectImporterFactory(ObjectImporterFactory objectImporterFactory) {
		this.objectImporterFactory = objectImporterFactory;
	}

	public void setObjectSchemaFactory(ObjectSchemaFactory objectSchemaFactory) {
		this.objectSchemaFactory = objectSchemaFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		objectSchemaFactory.registerSchema("com/krishagni/importcsv/core/schema/mskVisit.xml");
		objectImporterFactory.registerImporter("mskVisit", new MskVisitImporter());
	}
}