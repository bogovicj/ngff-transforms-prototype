package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.InvertibleRealTransform;

public interface InvertibleCoordinateTransform<T extends InvertibleRealTransform> extends RealCoordinateTransform<T> {

	public default InvertibleRealTransform getInverseTransform() {
		return getTransform().inverse();
	}

}
