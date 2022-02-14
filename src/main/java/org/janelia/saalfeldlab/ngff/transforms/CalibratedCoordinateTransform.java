package org.janelia.saalfeldlab.ngff.transforms;

import net.imglib2.realtransform.RealTransform;

public class CalibratedCoordinateTransform {

	private final RealCoordinateTransform transform;
	private final String unit;
	
	public CalibratedCoordinateTransform( final RealCoordinateTransform transform, final String unit) {
		this.transform = transform;
		this.unit = unit;
	}

	public RealCoordinateTransform getSpatialTransform() {

		return transform;
	}

	public RealTransform getTransform() {

		return transform.getTransform();
	}
	
	public String getUnit() {
		return unit;
	}

}
