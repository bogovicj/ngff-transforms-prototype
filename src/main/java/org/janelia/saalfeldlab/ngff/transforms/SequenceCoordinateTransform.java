package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;

public class SequenceCoordinateTransform extends AbstractCoordinateTransform {

	private final RealCoordinateTransform[] transformations;

	public SequenceCoordinateTransform( final String name, final RealCoordinateTransform[] transformations, 
			final String inputSpace, final String outputSpace ) {
		super("sequence", name, inputSpace, outputSpace );
		this.transformations = transformations;
	}

	@Override
	public RealTransform getTransform()
	{
		RealTransformSequence transform = new RealTransformSequence();
		for( RealCoordinateTransform t : getTransformations() )
			transform.add( t.getTransform() );

		return transform;
	}

	public RealCoordinateTransform[] getTransformations() {
		return transformations;
	}

}
