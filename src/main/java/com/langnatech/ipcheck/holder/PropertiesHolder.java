package com.langnatech.ipcheck.holder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class PropertiesHolder {
	private static final Logger logger = Logger.getLogger(PropertiesHolder.class);
	private static Configuration config = null;

	public static Configuration getConfig() {
		if (config == null) {
			try {
				config = new PropertiesConfiguration("ipcheck.properties");
			} catch (ConfigurationException e) {
				logger.error("get properties file [ipcheck.properties] failure!");
			}
		}
		return config;
	}
}
