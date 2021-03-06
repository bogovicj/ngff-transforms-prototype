package org.janelia.saalfeldlab.ngff.transforms;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.Compression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.ngff.spaces.Space;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.PositionFieldTransform;
import net.imglib2.transform.Transform;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class PositionFieldCoordinateTransform<T extends RealType<T>> extends AbstractParametrizedFieldTransform<PositionFieldTransform<T>,T> { 

	protected transient PositionFieldTransform<T> transform;
	
	protected transient int positionAxisIndex = 0;
	
	protected static final transient String vectorAxisType = "position";

	public PositionFieldCoordinateTransform( final String name, final RealRandomAccessible<T>[] fields, 
			final String inputSpace, final String outputSpace ) {
		super("position_field", name, null, inputSpace, outputSpace );
		buildTransform( fields );
	}

	public PositionFieldCoordinateTransform(final String name, final N5Reader n5, final String path,
			final String inputSpace, final String outputSpace) {
		super("position_field", name, path, inputSpace, outputSpace );
	}

	public PositionFieldCoordinateTransform( final String name, final String path,
			final String inputSpace, final String outputSpace) {
		super("position_field", name, path, inputSpace, outputSpace  );
	}

	public int getVectorAxisIndex() {
		return positionAxisIndex;
	}

	@Override
	public PositionFieldTransform<T> buildTransform( final RealRandomAccessible<T>[] realFields ) {
		return new PositionFieldTransform<>(realFields);
	}

	@Override
	public PositionFieldTransform<T> getTransform() {
		if( fields != null && transform == null )
			buildTransform(fields);

		return transform;
	}

	public int parseVectorAxisIndex( N5Reader n5 )
	{
		Space[] spaces; 
		try {
			spaces = n5.getAttribute(getParameterPath(), "spaces", Space[].class);

			final Space space = spaces[0];
			for( int i = 0; i < space.numDimensions(); i++ )
				if( space.getAxisTypes()[i].equals(vectorAxisType))
					return i;

		} catch (IOException e) { }	

		return -1;
	}
	
	public static <T extends RealType<T> & NativeType<T>> PositionFieldCoordinateTransform writePositionFieldTransform( 
			final N5Writer n5, final String dataset, final RandomAccessibleInterval<T> posField,
			final int[] blockSize, final Compression compression,
			 final Space[] spaces, final CoordinateTransform[] transforms ) {

		try {
			N5Utils.save(posField, n5, dataset, blockSize, compression);
			n5.setAttribute(dataset, "spaces", spaces);
			n5.setAttribute(dataset, "transformations", transforms);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
