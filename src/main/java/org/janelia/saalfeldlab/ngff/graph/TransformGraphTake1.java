package org.janelia.saalfeldlab.ngff.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.IdentityCoordinateTransform;

import com.google.gson.Gson;

public class TransformGraphTake1
{
	public static final Gson gson = new Gson();

	private final List< CoordinateTransform<?> > transforms;

	private final List< Space > spaces;

	private final HashMap< Space, SpaceNode > spacesToNodes;

	private final HashMap< String, Space > namesToSpaces;

	public TransformGraphTake1( List< CoordinateTransform<?> > transforms, 
			final List<Space> spacesIn ) {
		this.transforms = transforms;

		spaces = new ArrayList<>();
		spaces.addAll(spacesIn);
		spaces.add(Space.arraySpace(5));

		namesToSpaces = new HashMap<>();
		for( Space s : spaces )
			namesToSpaces.put( s.getName(), s );

		spacesToNodes = new HashMap< Space, SpaceNode >();
		for( CoordinateTransform<?> t : transforms )
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
		}
	}
	
	public List<CoordinateTransform<?>> getTransforms() {
		return transforms;
	}

	public Optional<CoordinateTransform<?>> getTransform( String name ) {
		return transforms.stream().filter( x -> x.getName().equals(name)).findAny();
	}

	public HashMap< Space, SpaceNode > getSpaces()
	{
		return spacesToNodes;
	}
	
	public HashMap<String, Space> getNamesToSpaces()
	{
		return namesToSpaces;
	}
	
	public Space getInputSpace( CoordinateTransform<?> t ) {
		return namesToSpaces.get( t.getInputSpace());
	}

	public Space getOutputSpace( CoordinateTransform<?> t ) {
		return namesToSpaces.get( t.getOutputSpace());
	}

	public Optional<TransformPath> path(final String from, final String to ) {
		return path( namesToSpaces.get(from), namesToSpaces.get(to));
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

		return allPaths( from ).stream().filter( p -> namesToSpaces.get(p.getEnd()).equals(to)).findAny();
	}

	public List<TransformPath> paths(final Space from, final Space to ) {

		return allPaths( from ).stream().filter( p -> p.getEnd().equals(to)).collect(Collectors.toList());
	}

	public List<TransformPath> allPaths(final String from) {
		return allPaths(namesToSpaces.get(from));
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
	
}
