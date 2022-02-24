package org.janelia.saalfeldlab.ngff.multiscales;

import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;

public class DatasetTransform {
	
	public String path;
	public CoordinateTransform<?>[] coordinateTransformations;

	public DatasetTransform( String path, 
			CoordinateTransform<?>[] coordinateTransformations)
	{
		this.path = path;
		this.coordinateTransformations = coordinateTransformations;
	}

	public DatasetTransform( String path, 
			CoordinateTransform<?> ct)
	{
		this.path = path;
		this.coordinateTransformations = new CoordinateTransform<?>[]{ ct };
	}

}
