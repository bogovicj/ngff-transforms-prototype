package org.janelia.saalfeldlab.ngff.transforms;

import java.util.ArrayList;

import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;

public abstract class AbstractCoordinateTransform<T> implements CoordinateTransform<T> {

	protected String type;

	protected String name;
	
	protected String input_space;

	protected String output_space;

	// implement
	protected String[] input_axes;

	protected String[] output_axes;

	protected transient Space inputSpaceObj;

	protected transient Space outputSpaceObj;

	public abstract T getTransform();

	public AbstractCoordinateTransform( final String type, 
			final String name,
			final String inputSpace, final String outputSpace ) {
		this.type = type;
		this.name = name;
		this.input_space = inputSpace;
		this.output_space = outputSpace;
	}
	
	public AbstractCoordinateTransform( final String type, 
			final String name,
			final String[] inputAxes, final String[] outputAxes ) {
		this.type = type;
		this.name = name;
		this.input_axes = inputAxes;
		this.output_axes = outputAxes;
	}

	/**
	 * 
	 * If this object does not have input_space or output_space defined,
	 * attempts to infer the space name, given * input_axes or output_axes, if they are defined.
	 * 
	 * 
	 * @param spaces the spaces object
	 * @return true if input_space and output_space are defined.
	 */
	public boolean inferSpacesFromAxes( Spaces spaces )
	{
		if( input_space == null && output_axes != null )
			input_space = spaceNameFromAxesLabels( spaces, input_axes );

		if( output_space == null && output_axes != null )
			output_space = spaceNameFromAxesLabels( spaces, output_axes );

		if( input_space != null && output_space != null )
			return true;
		else
			return false;
	}

	public String[] getInputAxes()
	{
		return input_axes;
	}

	public String[] getOutputAxes()
	{
		return output_axes;
	}

	private static String spaceNameFromAxesLabels( Spaces spaces, String[] axes )
	{
		ArrayList<Space> candidateSpaces = spaces.getSpacesFromAxes(axes);
		if( candidateSpaces.size() == 1 )
			return candidateSpaces.get(0).getName();
		else
			return null;
	}

	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getInputSpace() {
		return input_space;
	}

	@Override
	public String getOutputSpace() {
		return output_space;
	}
	
	public AbstractCoordinateTransform<T> setNameSpaces( String name, String in, String out )
	{
		this.name = name;
		this.input_space = in;
		this.output_space = out;
		return this;
	}
	
	public String toString()
	{
		return String.format("%s:(%s > %s)", name, input_space, output_space);
	}

}
