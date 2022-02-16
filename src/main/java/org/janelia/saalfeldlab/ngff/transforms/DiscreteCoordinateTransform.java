package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.transform.Transform;

public interface DiscreteCoordinateTransform<T extends Transform> extends CoordinateTransform<T> {

	@Override
	public T getTransform();
}
