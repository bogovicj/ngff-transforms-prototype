package org.janelia.saalfeldlab.ngff.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.AbstractCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.IdentityCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.InvertibleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.StackedCoordinateTransform;

import com.google.gson.Gson;

import net.imglib2.realtransform.InvertibleRealTransform;

public class TransformGraph
{
	public static final Gson gson = new Gson();

	private final ArrayList< CoordinateTransform<?> > transforms;
	
	private Spaces spaces;

	private final HashMap< Space, SpaceNode > spacesToNodes;

	public TransformGraph() {
		spaces = new Spaces();
		transforms = new ArrayList<>();
		spacesToNodes = new HashMap< Space, SpaceNode >();
	}

	public TransformGraph( List<CoordinateTransform<?>> transforms, final Spaces spaces ) {

		this.spaces = spaces;
		this.transforms = new ArrayList<>();
//		this.transforms.addAll(transforms);
//		this.transforms = transforms = new ArrayList<>();;
		inferSpacesFromAxes();

		spacesToNodes = new HashMap< Space, SpaceNode >();
		for( CoordinateTransform<?> t : transforms )
		{
			addTransform(t);

//			final Space src = getInputSpace( t );
//			if( spacesToNodes.containsKey( src ))
//				spacesToNodes.get( src ).edges().add( t );
//			else
//			{
//				SpaceNode node = new SpaceNode( src );
//				node.edges().add( t );
//				spacesToNodes.put( src, node );
//			}
		}
	}

	public TransformGraph( List< CoordinateTransform<?> > transforms, final List<Space> spacesIn ) {
		this( transforms, new Spaces(spacesIn) );
	}
	
	protected void inferSpacesFromAxes( )
	{
		for( CoordinateTransform<?> ct : transforms )
		{
			if( ct instanceof AbstractCoordinateTransform )
				if( ! ((AbstractCoordinateTransform)ct).inferSpacesFromAxes(spaces))
				{
					System.out.println( "uh oh - removing " + ct );
					transforms.remove(ct);
				}
		}
	}

	public List<CoordinateTransform<?>> getTransforms() {
		return transforms;
	}

	public Spaces getSpaces() {
		return spaces;
	}

	public Optional<CoordinateTransform<?>> getTransform( String name ) {
		return transforms.stream().filter( x -> x.getName().equals(name)).findAny();
	}

	public HashMap< Space, SpaceNode > getSpaceNodes() {
		return spacesToNodes;
	}

	public Space getInputSpace( CoordinateTransform<?> t ) {
//		return namesToSpaces.get( t.getInputSpace());
		return spaces.getSpace(t.getInputSpace());
	}

	public Space getOutputSpace( CoordinateTransform<?> t ) {
//		return namesToSpaces.get( t.getOutputSpace());
		return spaces.getSpace(t.getOutputSpace());
	}
	
	public void addTransform( CoordinateTransform<?> t ) {
		addTransform( t, true );
	}

	private void addTransform( CoordinateTransform<?> t, boolean addInverse ) {
		if( transforms.stream().anyMatch( x -> x.getName().equals(t.getName())) )
			return;

		if( spaces.hasSpace(t.getInputSpace()) && spaces.hasSpace(t.getOutputSpace()))
		{
			final Space src = getInputSpace( t );
			if( spacesToNodes.containsKey( src ))
				spacesToNodes.get( src ).edges().add( t );
			else
			{
				SpaceNode node = new SpaceNode( src );
				node.edges().add( t );
				spacesToNodes.put( src, node );
			}

			transforms.add(t);
		}
		
		if( addInverse && t instanceof InvertibleCoordinateTransform )
			addTransform( new InverseCT( (InvertibleCoordinateTransform) t ), false );
	}
	
//	private void addInverse( InvertibleCoordinateTransform<?> ict )
//	{
//		addTransform( new InverseCT(ict));
//	}

	public void addSpace( Space space )
	{
		if( spaces.add(space) ) {
			spacesToNodes.put( space, new SpaceNode(space));
		}
	}
	
	public void add( TransformGraph g )
	{
		g.spaces.spaces().forEach( s -> addSpace(s));
		g.transforms.stream().forEach( t -> addTransform(t));
	}

	/**
	 * Returns all transforms that have all input axis in the from space
	 * and all output axis in their to space.
	 * 
	 * @return
	 */
	public List<CoordinateTransform<?>> subTransforms( Space from, Space to) {
		return getTransforms().stream().filter( t -> 
			{
				return inputIsSubspace( t, from ) && outputIsSubspace( t, to );
			}
		).collect( Collectors.toList());
	}
	
	public CoordinateTransform<?> buildImpliedTransform( final Space from, final Space to )
	{
//		final List<CoordinateTransform<?>> tList = subTransforms( from, to );
		
		final List<CoordinateTransform<?>> tList = new ArrayList<>();

		// order list
		for( String outLabels : to.getAxisLabels() )
		{
			 getTransforms().stream().filter( t -> {
					return outputHasAxis( t, outLabels );
				}).findAny().ifPresent( t -> tList.add( t ));
		}

		StackedCoordinateTransform totalTransform = new StackedCoordinateTransform(
				"name", from.getName(), to.getName(), tList);

		return totalTransform;
	}

	public boolean inputIsSubspace( CoordinateTransform<?> t, Space s ) {
		return s.isSubspaceOf( spaces.getSpace(t.getInputSpace()));
	}

	public boolean inputHasAxis( CoordinateTransform<?> t, String axisLabel ) {
		return spaces.getSpace(t.getInputSpace()).hasAxis(axisLabel);
	}

	public boolean outputIsSubspace( CoordinateTransform<?> t, Space s ) {
		return s.isSubspaceOf( spaces.getSpace(t.getOutputSpace()));
	}

	public boolean outputHasAxis( CoordinateTransform<?> t, String axisLabel ) {
		return spaces.getSpace(t.getOutputSpace()).hasAxis(axisLabel);
	}
	
	public Space spaceFrom( String[] axes ) {
		return spaces.getSpaceFromAxes(axes);
	}

	public Space spaceFrom( String name ) {
		return spaces.getSpace(name);
	}

	public Optional<TransformPath> pathFromAxes(final String from, final String[] toAxes ) {
		return path( spaceFrom(from), spaceFrom(toAxes));
	}

	public Optional<TransformPath> pathFromAxes(final String[] fromAxes, final String to ) {
		return path( spaceFrom(fromAxes), spaceFrom(to));
	}

	public Optional<TransformPath> pathFromAxes(final String[] fromAxes, final String[] toAxes ) {
		return path( spaceFrom(fromAxes), spaceFrom(toAxes));
	}

	public Optional<TransformPath> path(final String from, final String to ) {
		return path( spaceFrom(from), spaceFrom(to));
	}

	public Optional<TransformPath> path(final Space from, final Space to ) {

		if( from == null || to == null )
			return Optional.empty();
		else if( from.equals(to))
			return Optional.of( new TransformPath(
					new IdentityCoordinateTransform("identity", from.getName(), to.getName())));


//		return allPaths( from ).stream().filter( p -> p.getEnd().equals(to))
//				.reduce( (x,y) -> {
//					if( x.getCost() < y.getCost() )
//						return x;
//					else
//						return y;
//				});

		return allPaths( from ).stream().filter( p -> spaces.getSpace(p.getEnd()).equals(to)).findAny();
	}

	public List<TransformPath> paths(final Space from, final Space to ) {

		return allPaths( from ).stream().filter( p -> p.getEnd().equals(to)).collect(Collectors.toList());
	}

	public List<TransformPath> allPaths(final String from) {
		return allPaths(spaces.getSpace(from));
	}

	public List<TransformPath> allPaths(final Space from) {

		final ArrayList<TransformPath> paths = new ArrayList<TransformPath>();
		allPathsHelper( paths, from, null );
		return paths;
	}

	private void allPathsHelper(final List<TransformPath> paths, final Space start, final TransformPath pathToStart) {

		SpaceNode node = spacesToNodes.get(start);

		List<CoordinateTransform<?>> edges = null;
		if( node != null )
			edges = spacesToNodes.get(start).edges();

		if( edges == null || edges.size() == 0 )
			return;

		for (CoordinateTransform<?> t : edges) {
			final Space end = getOutputSpace( t );
			
			if( pathToStart != null && pathToStart.hasSpace( end ))
				continue;

			final TransformPath p;
			if (pathToStart == null )
				p = new TransformPath( t );
			else
				p = new TransformPath( pathToStart, t );	

			paths.add(p);
			allPathsHelper(paths, end, p);
		}
	}
	
	private static class InverseCT extends AbstractCoordinateTransform<InvertibleRealTransform>
		implements InvertibleCoordinateTransform<InvertibleRealTransform> {

		InvertibleCoordinateTransform<?> ict;
		
		public InverseCT( InvertibleCoordinateTransform<?> ict ) {
			super("invWrap", "inv-" + ict.getName(), ict.getOutputSpace(), ict.getInputSpace());
			this.ict = ict;
		}

		public InverseCT(String type, String name, String inputSpace, String outputSpace, 
				InvertibleCoordinateTransform<?> ict ) {
			super(type, name, inputSpace, outputSpace);
			this.ict = ict;
		}

		@Override
		public InvertibleRealTransform getTransform() {
			return ict.getInverseTransform();
		} 

		@Override
		public InvertibleRealTransform getTransform( final N5Reader n5 ) {
			return ict.getInverseTransform( n5 );
		} 

		@Override
		public InvertibleRealTransform getInverseTransform() {
			return ict.getTransform();
		} 

		@Override
		public InvertibleRealTransform getTransform( N5Reader n5 ) {
			return ict.getInverseTransform( n5 );
		}

		@Override
		public InvertibleRealTransform getInverseTransform( N5Reader n5 ) {
			return ict.getTransform( n5 );
		}
	}
	
}
