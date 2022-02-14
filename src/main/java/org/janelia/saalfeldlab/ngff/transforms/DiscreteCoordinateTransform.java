package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.transform.Transform;

public interface DiscreteCoordinateTransform extends CoordinateTransform {

	@Override
	public Transform getTransform();
}
