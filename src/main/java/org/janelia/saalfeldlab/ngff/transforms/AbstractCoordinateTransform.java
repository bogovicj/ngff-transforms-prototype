package org.janelia.saalfeldlab.ngff.transforms;

public abstract class AbstractCoordinateTransform<T> implements CoordinateTransform<T> {

	private String type;

	private String name;
	
	private String input_space;

	private String output_space;

	// implement
//	private String[] input_axes;
//
//	private String[] output_axes;

	public abstract T getTransform();

	public AbstractCoordinateTransform( final String type, 
			final String name,
			final String inputSpace, final String outputSpace ) {
		this.type = type;
		this.name = name;
		this.input_space = inputSpace;
		this.output_space = outputSpace;
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
