/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2022 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.datastore.core;

import org.jdom2.Element;
import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.cache.CacheControl;
import lombok.RequiredArgsConstructor;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

@RequiredArgsConstructor
public class HPCDatastoreImageLoader implements ViewerImgLoader {

	final protected String baseUrl;

	@Parameter
	private Display<?> display;

	@Parameter
	private PluginService pluginService;

	private HPCDatastoreImageLoaderPlugin plugin;

	@Override
	public ViewerSetupImgLoader<?, ?> getSetupImgLoader(int setupId) {
		if (plugin == null) {
			throw new UnsupportedOperationException("This is only for XML export.");
		}
		return plugin.getSetupImgLoader(setupId);
	}

	@Override
	public CacheControl getCacheControl() {
		if (plugin == null) {
			throw new UnsupportedOperationException("This is only for XML export.");
		}
		return plugin.getCacheControl();
	}

	public HPCDatastoreImageLoader(String baseUrl,
		AbstractSequenceDescription<?, ?, ?> sequenceDescription, Element elem)
	{
		this(baseUrl);
		try (Context ctx = new Context()) {
			ctx.inject(this);
			if (pluginService != null) {
				plugin = pluginService.createInstancesOfType(
					HPCDatastoreImageLoaderPlugin.class).stream().findFirst().orElse(
						null);
				if (plugin != null) {
					plugin.init(baseUrl, sequenceDescription, elem);
				}
			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		
	}



}