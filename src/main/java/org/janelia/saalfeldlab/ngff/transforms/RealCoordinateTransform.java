package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.RealTransform;

public interface RealCoordinateTransform extends CoordinateTransform {

	@Override
	public RealTransform getTransform();
}
