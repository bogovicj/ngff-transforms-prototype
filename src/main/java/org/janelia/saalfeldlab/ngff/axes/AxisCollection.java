package org.janelia.saalfeldlab.ngff.axes;

import java.util.HashMap;

public class AxisCollection {

	private final HashMap<String,Axis> allAxes;
	
	public AxisCollection() {
		allAxes = new HashMap<>();
	}
	
	public void add( Axis axis ) {
		allAxes.put(axis.getLabel(), axis);
	}

}
