package org.janelia.saalfeldlab.ngff.spaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.ngff.axes.Axis;
import org.janelia.saalfeldlab.ngff.transforms.AbstractCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;

/**
 * This class manages Axes and Spaces.
 * 
 * @author John Bogovic
 *
 */
public class Spaces {

	private final static String PREFIX = "DEFAULTSPACE-";

	private HashMap<String,Space> nameToSpace;

	private HashMap<String,Axis> nameToAxis;

	private HashMap<Axis,ArrayList<Space>> axesToSpaces;

	public Spaces() {
		nameToSpace = new HashMap<>();
		nameToAxis = new HashMap<>();
		axesToSpaces = new HashMap<>();
	}

	public Spaces( Collection<Space> spaceList ) {
		this();
		addAll( spaceList );
	}

	public Spaces( Stream<Space> spaces ) {
		this();
		addAll( spaces );
	}

	public Stream<Space> spaces() {
		return nameToSpace.entrySet().stream().map(e -> e.getValue());
	}

	public Stream<Axis> axes() {
		return nameToAxis.entrySet().stream().map(e -> e.getValue());
	}

	public Axis[] axesFromLabels(String... axisLabels )
	{
		return Arrays.stream(axisLabels).map( l -> {
			return nameToAxis.get(l);
		}).toArray( Axis[]::new );
	}

	public Spaces( Space[] spaces ) {
		this();
		for( Space s : spaces )
			add( s );
	}

	public void addAll( Collection<Space> spaces )
	{
		for( Space s : spaces )
			add( s );
	}

	public void addAll( Stream<Space> spaces )
	{
		spaces.forEach( s -> add(s) );
	}

	/**
	 * 
	 * @param s the space to add
	 * @return true of the space was added
	 */
	public boolean add( Space s ) {

		if( nameToSpace.containsKey(s.getName()) ) {
			Space other = nameToSpace.get(s.getName());
			if( !s.equals(other))
				return false;
		}
		else
			nameToSpace.put( s.getName(), s);

		for( Axis a : s.getAxes() ) {
			if( add( a ))
			{
				ArrayList<Space> list = new ArrayList<Space>();
				list.add(s);
				axesToSpaces.put(a, list);
			}
		}
		return true;
	}

	public boolean add( Axis a ) {
		if( nameToAxis.containsKey(a.getLabel()) ) {
			Axis other = nameToAxis.get(a.getLabel());
			if( !a.equals(other))
				return false;
		}
		else
			nameToAxis.put(a.getLabel(), a);

		return true;
	}

	public Space getSpace( final String name ) {
		return nameToSpace.get(name);
	}
	
	public boolean hasSpace( final String name ) {
		return nameToSpace.containsKey(name);
	}

	public String defaultSpaceName( final String... axisLabels ) {
		return PREFIX + Arrays.stream(axisLabels).collect(Collectors.joining("-"));
	}

	public Space makeDefaultSpace( final String... axisLabels ) {
		return makeDefaultSpace( defaultSpaceName( axisLabels ), axisLabels );
	}

	public Space makeDefaultSpace( final String name, final String... axisLabels ) {
		Axis[] axes = new Axis[ axisLabels.length ];
		for( int i = 0; i < axisLabels.length; i++ ) {
			final Axis a = nameToAxis.get( axisLabels[i]);
			if( a != null ) {
				axes[i] = a;
			}
			else {
				return null;
			}
		}
		Space s =  new Space(name, axes);
		add( s );
		return s;
	}
	
	/**
	 * Returns the first space that contains exactly the given axis labels, in the given order.
	 * 
	 * @param axisLabels the axis labels
	 * @return the list of spaces
	 */
	public Space getSpaceFromAxes( final String... axisLabels ) {
		return getSpacesFromAxes(axisLabels).get(0);
	}
	
	/**
	 * Returns all spaces that contains exactly the given axis labels, in the given order.
	 * 
	 * @param axisLabels the axis labels
	 * @return the list of spaces
	 */
	public ArrayList<Space> getSpacesFromAxes( final String... axisLabels ) {
		final ArrayList<Space> list = new ArrayList<Space>(
				spaces()
				.filter( s -> s.axesLabelsMatch(axisLabels))
				.collect( Collectors.toList()));

		if( list.isEmpty())
			list.add(makeDefaultSpace(axisLabels));	

		return list;
	}

	/**
	 * Returns all spaces that contain the given axis labels, in any order.
	 * Spaces in the returned list may contain axes not in the list.
	 * 
	 * @param axisLabels the axis labels
	 * @return the list of spaces
	 */
	public ArrayList<Space> getSpacesContainingAxes( final String... axisLabels ) {
		final ArrayList<Space> list = new ArrayList<Space>(
				spaces()
				.filter( s -> s.hasAllLabels(axisLabels))
				.collect( Collectors.toList()));

		if( list.isEmpty())
			list.add(makeDefaultSpace(axisLabels));	

		return list;
	}

	public ArrayList<Space> getSpacesOld( final String... axisLabels ) {
		ArrayList<Space> candidates = null;
		for( final String l : axisLabels )
		{
			final Axis a = nameToAxis.get(l);
			if( a != null ) {
				ArrayList<Space> spaces = axesToSpaces.get(a);
				if( candidates == null) {
					candidates = new ArrayList<>();
					candidates.addAll(spaces);
				}
				else {
					for( Space s : spaces )
						if( !candidates.contains(s))
							candidates.remove(s);
				}
			}
		}

		if( candidates.isEmpty())
			candidates.add(makeDefaultSpace(axisLabels));

		return candidates;
	}

	public Axis getAxis( final String name ) {
		return nameToAxis.get(name);
	}

	public Space spaceFrom( String[] axes ) {
		return getSpaceFromAxes(axes);
	}

	public Space spaceFrom( String name ) {
		return getSpace(name);
	}

	public String[] getInputAxes( CoordinateTransform<?> t )
	{
		String[] inAxes = null;
		if( t.getInputAxes() != null )
			inAxes = t.getInputAxes();
		else if( t.getInputSpace() != null )
			inAxes = getSpace(t.getInputSpace()).getAxisLabels();

		return inAxes;
	}

	public String[] getOutputAxes( CoordinateTransform<?> t )
	{
		String[] outAxes = null;
		if( t.getOutputAxes() != null )
			outAxes = t.getOutputAxes();
		else if( t.getOutputSpace() != null )
			outAxes = getSpace(t.getOutputSpace()).getAxisLabels();

		return outAxes;
	}

	public boolean inputIsSubspace( CoordinateTransform<?> t, Space s ) {
		return s.isSubspaceOf( getSpace(t.getInputSpace()));
	}

	public boolean inputHasAxis( CoordinateTransform<?> t, String axisLabel ) {
		return getSpace(t.getInputSpace()).hasAxis(axisLabel);
	}

	public boolean outputIsSubspace( CoordinateTransform<?> t, Space s ) {
		if ( t.getOutputAxes() != null )
			return s.isSubspaceOf(t.getOutputAxes());
		else if( t.getOutputSpace() != null )
			return s.isSubspaceOf( getSpace(t.getOutputSpace()));
		else
			return false;
	}
	
	public boolean outputIsSuperspace( CoordinateTransform<?> t, Space s ) {
		if ( t.getOutputAxes() != null )
			return s.isSuperspaceOf(t.getOutputAxes());
		else if( t.getOutputSpace() != null )
			return s.isSuperspaceOf( getSpace(t.getOutputSpace()));
		else
			return false;
	}

	public boolean outputHasAxis( CoordinateTransform<?> t, String axisLabel ) {
		if( t.getOutputAxes() != null )
			return Arrays.stream( t.getOutputAxes() ).anyMatch( x -> x.equals(axisLabel));
		else if( t.getOutputSpace() != null )
			return getSpace(t.getOutputSpace()).hasAxis(axisLabel);
		else
			return false;
	}
	
	public boolean outputMatchesAny( CoordinateTransform<?> t, Set<String> axisLabels ) {
		String[] outAxes = null;
		if( t.getOutputAxes() != null )
			outAxes = t.getOutputAxes();
		else if( t.getOutputSpace() != null )
			outAxes = getSpace(t.getOutputSpace()).getAxisLabels();
		else
			return false;

		for( String axisLabel : t.getOutputAxes())
			if( axisLabels.contains(axisLabel ))
				return true;

		return false;
	}

	public boolean outputMatchesAny( CoordinateTransform<?> t, String[] axisLabels ) {
		String[] outAxes = null;
		if( t.getOutputAxes() != null )
			outAxes = t.getOutputAxes();
		else if( t.getOutputSpace() != null )
			outAxes = getSpace(t.getOutputSpace()).getAxisLabels();
		else
			return false;

		for( String axisLabel : t.getOutputAxes())
			if( Arrays.stream( axisLabels ).filter( x -> x.equals(axisLabel)).count() > 0 )
				return true;

		return false;
	}
	
	public <T extends AbstractCoordinateTransform<?>> void updateTransforms( Stream<T> transforms )
	{
		transforms.forEach( t -> {
			if( t.getInputSpace() != null )
				t.setInputSpace( nameToSpace.get( t.getInputSpace() ));
			else if ( t.getInputAxes() != null )
				t.setInputSpace( new Space("", axesFromLabels( t.getInputAxes()) ));

			if( t.getOutputSpace() != null )
				t.setOutputSpace( nameToSpace.get( t.getOutputSpace() ));
			else if ( t.getOutputAxes() != null )
				t.setOutputSpace( new Space("", axesFromLabels( t.getOutputAxes()) ));
		});
		
	}

}
