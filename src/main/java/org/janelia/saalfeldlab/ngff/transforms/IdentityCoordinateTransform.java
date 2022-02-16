package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;

public class IdentityCoordinateTransform extends AbstractCoordinateTransform<RealTransform> {

	public IdentityCoordinateTransform( final String name, final String inputSpace, final String outputSpace ) {
		super("identity", name, inputSpace, outputSpace );
	}

	@Override
	public RealTransform getTransform()
	{
		// an empty RealTransformSequence is the identity
		return new RealTransformSequence();
	}
	
}
