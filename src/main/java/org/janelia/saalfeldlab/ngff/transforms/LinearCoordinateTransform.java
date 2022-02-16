package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.AffineGet;

public interface LinearCoordinateTransform<T extends AffineGet> extends RealCoordinateTransform<T> {

	@Override
	public T getTransform();
}
