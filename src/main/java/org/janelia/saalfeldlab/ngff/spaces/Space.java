package org.janelia.saalfeldlab.ngff.spaces;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.janelia.saalfeldlab.ngff.axes.Axis;

/**
 * A Space is a set of axes.
 *
 * @author John Bogovic
 */
public class Space {

	private final String name;

	private final Axis[] axes;
	
	public Space( String name, Axis[] axes )
	{
		this.name = name;
		this.axes = axes;
	}

	public String getName() {
		return name;
	}

	public Axis[] getAxes() {
		return axes;
	}

	public String[] getAxisLabels() {
		return Arrays.stream(axes).map( Axis::getLabel).toArray(String[]::new);
	}

	public String[] getAxisTypes() {
		return Arrays.stream(axes).map( Axis::getType).toArray(String[]::new);
	}

	public String[] getUnits() {
		return Arrays.stream(axes).map( Axis::getUnit).toArray(String[]::new);
	}

	public Axis getAxis( int i ) {
		return axes[i];
	}


	/**
	 * 
	 * @param label the label
	 * @return the first index corresponding to that label
	 */
	public int indexOf(String label) {
		for (int i = 0; i < getAxisLabels().length; i++)
			if (getAxisLabels()[i].equals(label))
				return i;

		return -1;
	}

	public int[] indexesOfType( final String type ) {
		String[] types = getAxisTypes();
		return IntStream.range(0, getAxisTypes().length )
			.filter( i -> types[i].equals(type))
			.toArray();
	}
	
	public boolean equals( Object other )
	{
		// TODO should probably make this more strict 
		// but fine for prototype
		if( other instanceof Space )
		{
			final Space s = (Space)other;
			if( s.axes.length != axes.length )
				return false;

			for( int i = 0; i < axes.length; i++ )
				if( !axes[i].getLabel().equals( s.axes[i].getLabel() ))
					return false;

			return true;
		}
		else 
			return false;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static final Space arraySpace( int nd )
	{
		return new Space("", 
				IntStream.range(0, nd).mapToObj( i -> Axis.arrayAxis(i) ).toArray( Axis[]::new ));
	}

}
