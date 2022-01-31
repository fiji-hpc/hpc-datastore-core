/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2022 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.datastore.core;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

import java.io.File;

import org.jdom2.Element;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

@ImgLoaderIo(format = "bdv.hpc-datastore", type = HPCDatastoreImageLoader.class)
public class XmlIoHPCDatastoreImageLoader implements
	XmlIoBasicImgLoader<HPCDatastoreImageLoader>
{

	public XmlIoHPCDatastoreImageLoader() {

	}

	@Override
	public Element toXml(final HPCDatastoreImageLoader imgLoader, final File basePath) {
		final Element elem = new Element("ImageLoader");
		elem.setAttribute(IMGLOADER_FORMAT_ATTRIBUTE_NAME, "bdv.hpc-datastore");
		elem.addContent(XmlHelpers.textElement("baseUrl", imgLoader.baseUrl));
		return elem;
	}

	@Override
	public HPCDatastoreImageLoader fromXml(final Element elem, final File basePath,
		final AbstractSequenceDescription<?, ?, ?> sequenceDescription)
	{
		final String baseUrl = elem.getChildText("baseUrl");
		return new HPCDatastoreImageLoader(baseUrl, sequenceDescription, elem);
	}

}

