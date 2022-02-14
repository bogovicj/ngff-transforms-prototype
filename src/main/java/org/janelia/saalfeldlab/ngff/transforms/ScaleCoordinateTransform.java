package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;

import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.Scale2D;
import net.imglib2.realtransform.Scale3D;

public class ScaleCoordinateTransform extends AbstractLinearCoordinateTransform<double[],ScaleCoordinateTransform> {

	public double[] scale;

	public transient AffineGet transform;

	public ScaleCoordinateTransform( String name, final String inputSpace, final String outputSpace,
			final double[] scale) {
		super("scale", name, inputSpace, outputSpace );
		this.scale = scale;
		buildTransform( scale );
	}

	public ScaleCoordinateTransform( final String name, final String inputSpace, final String outputSpace,
		 final N5Reader n5, final String path ) {
		super("scale", name, path, inputSpace, outputSpace );
		this.scale = getParameters(n5);
		buildTransform( scale );
	}

	public ScaleCoordinateTransform( final String name, final String inputSpace, final String outputSpace,
			final String path ) {
		super("scale", name, path, inputSpace, outputSpace );
		this.scale = null;
	}

	@Override
	public AffineGet buildTransform( double[] parameters )
	{
		this.scale = parameters;
		if( parameters.length == 2 )
			transform = new Scale2D(parameters);
		else if( parameters.length == 3 )
			transform = new Scale3D(parameters);
		else
			transform = new Scale(parameters);

		return transform;
	}

	@Override
	public AffineGet getTransform() {
		if( transform == null && scale != null )
			buildTransform(scale);

		return transform;
	}

	@Override
	public double[] getParameters(N5Reader n5) {
		return getDoubleArray( n5 , getParameterPath() );
	}
	
}
