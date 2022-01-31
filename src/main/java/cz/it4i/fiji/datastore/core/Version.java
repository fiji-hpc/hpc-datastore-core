/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2022 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.datastore.core;

import mpicbg.spim.data.generic.base.NamedEntity;


public class Version extends NamedEntity implements Comparable<Version> {

	public static int stringToIntVersion(final String versionStr) {
		if (versionStr.equals("mixedLatest")) {
			return -1;
		}
		return Integer.parseInt(versionStr);
	}

	public Version(int value) {
		super(value, "" + value);
	}

	@Override
	public int compareTo(Version o) {
		return Integer.compare(this.getId(), o.getId());
	}

	protected Version() {

	}
}
