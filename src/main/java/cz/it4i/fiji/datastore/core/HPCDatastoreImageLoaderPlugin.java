/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2022 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.datastore.core;

import org.jdom2.Element;
import org.scijava.plugin.SciJavaPlugin;

import bdv.ViewerImgLoader;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;

public interface HPCDatastoreImageLoaderPlugin extends SciJavaPlugin,
	ViewerImgLoader
{

	void init(String baseUrl,
		AbstractSequenceDescription<?, ?, ?> sequenceDescription, Element elem)
		throws SpimDataException;
}
