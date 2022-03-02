package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;

import net.imglib2.realtransform.RealTransformSequence;

public class SequenceCoordinateTransform extends AbstractCoordinateTransform<RealTransformSequence> {

	private final RealCoordinateTransform<?>[] transformations;

	public SequenceCoordinateTransform( final String name, final String inputSpace, final String outputSpace,
		final RealCoordinateTransform<?>[] transformations) {
		super("sequence", name, inputSpace, outputSpace );
		this.transformations = transformations;
	}

	@Override
	public RealTransformSequence getTransform()
	{
		RealTransformSequence transform = new RealTransformSequence();
		for( RealCoordinateTransform<?> t : getTransformations() )
			transform.add( t.getTransform() );

		return transform;
	}

	@Override
	public RealTransformSequence getTransform(final N5Reader n5 )
	{
		RealTransformSequence transform = new RealTransformSequence();
		for( RealCoordinateTransform<?> t : getTransformations() )
			transform.add( t.getTransform(n5) );

		return transform;
	}

	public RealCoordinateTransform<?>[] getTransformations() {
		return transformations;
	}

}
