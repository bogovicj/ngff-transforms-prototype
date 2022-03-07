package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;

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

	public RealCoordinateTransform<?> getForward() {
		return forward;
	}

	public RealCoordinateTransform<?> getInverse() {
		return inverse;
	}

	@Override
	public ExplicitInvertibleRealTransform getTransform()
	{
		return new ExplicitInvertibleRealTransform(forward.getTransform(), inverse.getTransform());
	}
	
	@Override
	public ExplicitInvertibleRealTransform getTransform(N5Reader n5)
	{
		return new ExplicitInvertibleRealTransform(forward.getTransform(n5), inverse.getTransform(n5));
	}

}
