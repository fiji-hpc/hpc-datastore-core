/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2021 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.datastore.core;


/*
 * #%L
 * BigDataViewer core classes with minimal dependencies.
 * %%
 * Copyright (C) 2012 - 2020 BigDataViewer developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import net.imglib2.realtransform.AffineTransform3D;

import org.janelia.saalfeldlab.n5.DataType;

import bdv.img.hdf5.DimsAndExistence;
import bdv.img.hdf5.MipmapInfo;
import bdv.img.hdf5.ViewLevelId;
import lombok.Getter;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.TimePoint;

@Getter
public class HPCDatastoreImageLoaderMetaData
{

	String uuid;

	/**
	 * The highest occurring timepoint id + 1. This is the maximum number of
	 * timepoints that could possibly exist.
	 */
	int maxNumTimepoints;

	/**
	 * The highest occurring setup id + 1. This is the maximum number of setups
	 * that could possibly exist.
	 */
	int maxNumSetups;

	/**
	 * The maximum number of mipmap levels occuring in all setups.
	 */
	int maxNumLevels;

	/**
	 * Description of available mipmap levels for each {@link BasicViewSetup}.
	 * Contains for each mipmap level, the subsampling factors and subdivision
	 * block sizes. The {@link HashMap} key is the setup id.
	 */
	final Map<Integer, MipmapInfo> perSetupMipmapInfo;

	/**
	 * Maps {@link ViewLevelId} (timepoint, setup, level) to
	 * {@link DimsAndExistence}. Every entry represents the existence and
	 * dimensions of one image.
	 */
	final Map<ViewLevelId, DimsAndExistence> dimsAndExistence;

	final DataType dataType;

	public HPCDatastoreImageLoaderMetaData(
		final AbstractSequenceDescription<?, ?, ?> sequenceDescription,
		Map<Integer, MipmapInfo> aPerSetupMipmapInfo,
		Function<ViewLevelId, DimsAndExistence> dims, DataType aDataType)
	{
		perSetupMipmapInfo = new HashMap<>(aPerSetupMipmapInfo);
		dimsAndExistence = new HashMap<>();
		dataType = aDataType;

		final List< TimePoint > timepoints = sequenceDescription.getTimePoints().getTimePointsOrdered();
		maxNumTimepoints = timepoints.get( timepoints.size() - 1 ).getId() + 1;

		final List< ? extends BasicViewSetup > setups = sequenceDescription.getViewSetupsOrdered();
		maxNumSetups = setups.get( setups.size() - 1 ).getId() + 1;

		maxNumLevels = 0;

		for ( final BasicViewSetup setup : setups )
		{
			final int setupId = setup.getId();
			final MipmapInfo info = perSetupMipmapInfo.get(setupId);

			final int numLevels = info.getNumLevels();
			if ( numLevels > maxNumLevels )
				maxNumLevels = numLevels;

			for ( final TimePoint timepoint : timepoints )
			{
				final int timepointId = timepoint.getId();
				for ( int level = 0; level < numLevels; ++level )
				{
					final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
					dimsAndExistence.put(id, dims.apply(id));
				}
			}
		}
	}

	public HPCDatastoreImageLoaderMetaData(DatasetDTO datasetDTO,
		AbstractSequenceDescription<?, ?, ?> sequenceDescription,
		DataType aDataType)
	{
		uuid = datasetDTO.getUuid();

		perSetupMipmapInfo = createPerSetupMipmapInfo(sequenceDescription
			.getViewSetupsOrdered(), datasetDTO);
		dimsAndExistence = new HashMap<>();
		dataType = aDataType;

		final List<TimePoint> timepoints = sequenceDescription.getTimePoints()
			.getTimePointsOrdered();
		maxNumTimepoints = timepoints.get(timepoints.size() - 1).getId() + 1;

		final List<? extends BasicViewSetup> setups = sequenceDescription
			.getViewSetupsOrdered();
		maxNumSetups = setups.get(setups.size() - 1).getId() + 1;

		maxNumLevels = 0;

		for (final BasicViewSetup setup : setups) {
			final int setupId = setup.getId();
			final MipmapInfo info = perSetupMipmapInfo.get(setupId);

			final int numLevels = info.getNumLevels();
			if (numLevels > maxNumLevels) maxNumLevels = numLevels;

			for (final TimePoint timepoint : timepoints) {
				final int timepointId = timepoint.getId();
				for (int level = 0; level < numLevels; ++level) {
					final ViewLevelId id = new ViewLevelId(timepointId, setupId, level);
					DimsAndExistence temp = new DimsAndExistence(
						getDimensions(datasetDTO.getDimensions(), datasetDTO
							.getResolutionLevels()[level].getResolutions()), true);
					dimsAndExistence.put(id, temp);
				}
			}
		}
	}

	/**
	 * Create an map from {@link ViewLevelId} (timepoint, setup, level) to
	 * int[]. Every entry is the dimensions in cells (instead of pixels) of one
	 * image.
	 */
	public Map<ViewLevelId, int[]> createCellsDimensions()
	{
		final Map<ViewLevelId, int[]> cellsDimensions = new HashMap<>();
		for ( final Entry< ViewLevelId, DimsAndExistence > entry : dimsAndExistence.entrySet() )
		{
			final ViewLevelId id = entry.getKey();
			final long[] imageDimensions = entry.getValue().getDimensions();
			final int[] cellSize = perSetupMipmapInfo.get( id.getViewSetupId() ).getSubdivisions()[ id.getLevel() ];
			final int[] dims = new int[] {
					( int ) ( imageDimensions[ 0 ] + cellSize[ 0 ] - 1 ) / cellSize[ 0 ],
					( int ) ( imageDimensions[ 1 ] + cellSize[ 1 ] - 1 ) / cellSize[ 1 ],
					( int ) ( imageDimensions[ 2 ] + cellSize[ 2 ] - 1 ) / cellSize[ 2 ] };
			cellsDimensions.put( id, dims );
		}
		return cellsDimensions;
	}

	public static Map<Integer, MipmapInfo> createPerSetupMipmapInfo(
		List<? extends BasicViewSetup> viewSetups, DatasetDTO dataset)
	{

		Map<Integer, MipmapInfo> result = new HashMap<>();
		MipmapInfo exportInfo = MipmapInfoAssembler.createExportMipmapInfo(dataset);
		for (BasicViewSetup viewSetup : viewSetups)
		{
			AffineTransform3D[] transforms = new AffineTransform3D[dataset
				.getResolutionLevels().length];
			AffineTransform3D affine = new AffineTransform3D();

			for (int i = 0; i < transforms.length; i++) {
				transforms[i] = affine.copy();
				double[] level = exportInfo.getResolutions()[i];
				transforms[i].scale(level[0], level[1], level[2]);
				// translate for center upper level
				transforms[i].translate(level[0] / 2 - 0.5, level[1] / 2 - 0.5,
					level[2] / 2 - 0.5);
			}
			MipmapInfo info = new MipmapInfo(exportInfo.getResolutions(), transforms,
				exportInfo.getSubdivisions());
			result.put(viewSetup.getId(), info);

		}
		return result;
	}

	private static long[] getDimensions(long[] dimensions, int[] resolution) {
		long[] result = new long[dimensions.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = dimensions[i] / resolution[i];
		}
		return result;
	}

}

