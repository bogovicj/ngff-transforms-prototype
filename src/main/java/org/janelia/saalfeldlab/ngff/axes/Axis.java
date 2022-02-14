package org.janelia.saalfeldlab.ngff.axes;

public class Axis {

	private final String type;

	private final String label;

	private final String unit;

	private final boolean discrete;

	public Axis( final String label, final String type, final String unit,
			final boolean discrete )
	{
		this.type = type;
		this.label = label;
		this.unit = unit;
		this.discrete = false;
	}

	public Axis( final String label, final String type, final String unit)
	{
		this( label, type, unit, false );
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public String getUnit() {
		return unit;
	}

	public boolean isDiscrete() {
		return discrete;
	}
	
	public static final Axis arrayAxis( int i )
	{
		return new Axis( String.format("dim_%d", i), "", "", true );
	}

}
