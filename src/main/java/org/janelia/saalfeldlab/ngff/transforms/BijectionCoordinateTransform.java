package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.ExplicitInvertibleRealTransform;

public class BijectionCoordinateTransform extends AbstractCoordinateTransform<ExplicitInvertibleRealTransform> 
	implements InvertibleCoordinateTransform<ExplicitInvertibleRealTransform> {

	private final RealCoordinateTransform<?> forward;

	private final RealCoordinateTransform<?> inverse;

	public BijectionCoordinateTransform( final String name, final String inputSpace, final String outputSpace,
		final RealCoordinateTransform<?> forward, final RealCoordinateTransform<?> inverse) {
		super("bijection", name, inputSpace, outputSpace );
		this.forward = forward;
		this.inverse = inverse;
	}

	@Override
	public ExplicitInvertibleRealTransform getTransform()
	{
		return new ExplicitInvertibleRealTransform(forward.getTransform(), inverse.getTransform());
	}

}
