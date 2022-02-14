package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.AffineGet;

public interface LinearCoordinateTransform extends RealCoordinateTransform {

	@Override
	public AffineGet getTransform();
}
