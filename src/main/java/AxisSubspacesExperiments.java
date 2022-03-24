import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.ngff.SpacesTransforms;
import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.ArraySpace;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.StackedCoordinateTransform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.componenttree.BuildComponentTree;
import net.imglib2.realtransform.RealComponentMappingTransform;
import net.imglib2.realtransform.RealInvertibleComponentMappingTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.Scale2D;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.Translation3D;

public class AxisSubspacesExperiments {

	public static void main(String[] args) throws FileNotFoundException {
//		final String testDataF = "/home/john/dev/ngff/ngff-transforms-prototype/src/test/resources/XandYtoXY.json";
//		final String testDataF = "src/test/resources/XY.json";
//		final String testDataF = "src/test/resources/sepXY.json";
//		final String testDataF = "src/test/resources/XcrossY.json";
//		final String testDataF = "src/test/resources/XcrossY.json";
//		final String testDataF = "src/test/resources/stack.json";
//		final String testDataF = "src/test/resources/noStack.json";
		final String testDataF = "src/test/resources/multiLevelAxisGraph.json";

		SpacesTransforms st = SpacesTransforms.loadFile(testDataF);
		System.out.println( st );

//		TransformGraph tgraph = st.buildTransformGraph( 2 );
//		transformsTest( tgraph );

//		stackedTest( st );

//		stackedSubspaceTest( st );

//		stackedTransformTest();

//		permutationTest();
		
//		subspaceNoStackTest( st );

		multilevelAxisGraphTest( st );
	}

	public static void multilevelAxisGraphTest( SpacesTransforms st  )
	{
		System.out.println( "multilevelAxisGraphTest" );
		System.out.println( st );

		TransformGraph g = st.buildTransformGraph(5);
		System.out.println( g );

		Space arraySpace = g.getSpaces().getSpace("");
		Space xyczt = g.getSpaces().getSpace("xyczt");

		CoordinateTransform<?> xfm = buildTransformFromAxes( 
				g.getSpaces(), Arrays.asList(st.transforms),
				arraySpace, xyczt );

		System.out.println( xfm );
		
	}

	public static void subspaceNoStackTest( SpacesTransforms st  )
	{
		System.out.println("subspaceNoStackTest" );

		TransformGraph g = st.buildTransformGraph(5);
		System.out.println( g );

		Optional<TransformPath> path = g.path("", "xyczt");
//		p.ifPresent( System.out::println );
		System.out.println( path ); // this optional is empty
		System.out.println( "" );
		
		Space arraySpace = g.getSpaces().getSpace("");
		Space xyczt = g.getSpaces().getSpace("xyczt");
		System.out.println( arraySpace );
		System.out.println( xyczt );

//		CoordinateTransform<?> xfm = g.buildImpliedTransform( arraySpace, xyczt );
//		System.out.println( xfm );

		CoordinateTransform<?> xfm = g.buildTransformFromAxes( arraySpace, xyczt );
		System.out.println( xfm );

		RealPoint p = new RealPoint( -1.0, 1.0, 2.0, 3.0, 4.0 );
		RealPoint q = new RealPoint( 0.0, 0.0, 0.0, 0.0, 0.0 );
		
		((RealTransform)xfm.getTransform()).apply(p, q);
		System.out.println( "p: " + p );
		System.out.println( "q: " + q );


//		println( g.getTransforms().stream().count() );
//		
//		for( String l : xyczt.getAxisLabels() )
//		{
//			System.out.println( l );
//			 Stream<CoordinateTransform<?>> ttmp = g.getTransforms().stream().filter( t -> {
//					return g.outputHasAxis( t, l );
//				});
//			 ttmp.forEach( x -> println(x) );
//
//			 System.out.println( " " );
//		}

//		g.getTransforms().forEach( System.out::println );


	}
	
	public static void println( Object o )
	{
		System.out.println( o );
	}

	public static void permutationTest()
	{
		// test coordinate mapping and inverse first
		String[] a = new String[]{ "c", "t", "x", "y", "z" };
		String[] b = new String[]{ "x", "y", "c", "z", "t"};
		
		int[] pf = AxisUtils.findPermutation(a, b);
		int[] pi = AxisUtils.findPermutation(b, a);
		
		RealPoint p = new RealPoint( 0.0, 1.0, 2.0, 3.0, 4.0 );
		RealPoint q = new RealPoint( 0.0, 0.0, 0.0, 0.0, 0.0 );

		// this works
//		RealInvertibleComponentMappingTransform perm = new RealInvertibleComponentMappingTransform(pf);
//		perm.apply(p, q);
//
//		System.out.println( "p: " + p );
//		System.out.println( "q: " + q );
//		System.out.println( " " );
//		p.setPosition(new double[]{-1, -1, -1, -1, -1 }); // to make sure p changes
//		
//		perm.applyInverse( p, q );
//		System.out.println( "q: " + q );
//		System.out.println( "p: " + p );
		
		// these work
//		RealComponentMappingTransform fwd = new RealComponentMappingTransform(5, pf);
//		RealComponentMappingTransform inv = new RealComponentMappingTransform(5, pi);
//		
//		fwd.apply(p, q);
//
//		System.out.println( "p: " + p );
//		System.out.println( "q: " + q );
//		System.out.println( " " );
//		
//		inv.apply( q, p );
//		System.out.println( "q: " + q );
//		System.out.println( "p: " + p );

	}
	
	public static void stackedTransformTest()
	{
		Scale2D s = new Scale2D( 2.0, 3.0 );
		Translation3D t = new Translation3D( -5, -6, -7 );
		StackedRealTransform xfm = new StackedRealTransform(s, t);

		RealPoint p = new RealPoint( 1.0, -1.0, 0.0, 0.0, 0.0 );
		RealPoint q = new RealPoint( 0.0, 0.0, 0.0, 0.0, 0.0 );
		
		xfm.apply( p, q );
		System.out.println( " " );
		System.out.println( "p: " + p );
		System.out.println( "q: " + q );
	}

	public static void stackedSubspaceTest( SpacesTransforms st ) {
		TransformGraph tgraph = st.buildTransformGraph( 5 );
		System.out.println( tgraph );
		StackedCoordinateTransform xfm = (StackedCoordinateTransform) tgraph.getTransform("stack").get();
		System.out.println( xfm );
		
		xfm.setSpaces(tgraph.getSpaces());
		xfm.buildTransform();
		
		//                             x    y    c    z    t
		RealPoint p = new RealPoint( 1.0, 1.0, 1.0, 1.0, 1.0 );
		RealPoint q = new RealPoint( 0.0, 0.0, 0.0, 0.0, 0.0 );
		xfm.getTransform().apply(p, q);

		System.out.println( " " );
		System.out.println( "p: " + p );
		System.out.println( "q: " + q );
	}

	// with XcrossY
	public static void stackedTest( SpacesTransforms st ) {
		StackedCoordinateTransform stackedXfm = (StackedCoordinateTransform) st.transforms[0];
		System.out.println( stackedXfm );
		RealTransform t = stackedXfm.getTransform();
		
		RealPoint p = new RealPoint( 1.0, 1.0 );
		RealPoint q = new RealPoint( 0.0, 0.0 );
		
		t.apply(p, q);
		System.out.println( "" );
		System.out.println( p );
		System.out.println( q );

	}

	// with sepXY
	public static void transformsTest( TransformGraph tGraph ) {
		System.out.println( tGraph );
		System.out.println( "" );

		Space arraySpace = tGraph.getSpaces().getSpace("");
		Space xySpace = tGraph.getSpaces().getSpaceFromAxes("x", "y");
		System.out.println( arraySpace );
		System.out.println( xySpace );
		
		
		// expect empty
		Optional<TransformPath> pdxy = tGraph.path("", "DEFAULT-x-y");
		System.out.println( pdxy );

//		Optional<TransformPath> pdxy = tGraph.path( new String[]{"dim_0", "dim-1"}, new String[] {"x", "y"]);

//		List<CoordinateTransform<?>> subTransforms = tGraph.subTransforms(arraySpace, xySpace);
//		System.out.println( subTransforms.size() );
//		for( CoordinateTransform ct : subTransforms )
//		{
//			System.out.println( ct.getName());
//		}
		
//		for( CoordinateTransform t : tGraph.getTransforms())
//		{
//			boolean sfrom = tGraph.inputIsSubspace( t, arraySpace ) ;
//			boolean sto = tGraph.outputIsSubspace( t, xySpace );
//			System.out.println( "");
//			System.out.println( t.getName());
//			System.out.println( sfrom );
//			System.out.println( sto );
//		}

		CoordinateTransform<?> total = tGraph.buildImpliedTransform(arraySpace, xySpace);
		System.out.println( total );
	}
	
	public static void spacesTest( SpacesTransforms st ) {
		Spaces s = new Spaces( st.spaces );

		System.out.println("spaces:");
		s.spaces().forEach(System.out::println);
		System.out.println("");
		System.out.println("axes:");
		s.axes().forEach(System.out::println);
		
		ArrayList<Space> xspaces = s.getSpacesFromAxes("x");
		System.out.println( "" );
		System.out.println( "space \"x\"" );
		System.out.println( xspaces.size());
		System.out.println( xspaces.get(0));

		ArrayList<Space> xyspaces = s.getSpacesFromAxes("x", "y");
		System.out.println( "" );
		System.out.println( "space \"xy\"" );
		System.out.println( xyspaces.size());
		System.out.println( xyspaces.get(0));
	}
	
	public static CoordinateTransform<?> buildTransformFromAxes( 
			Spaces spaces, List<CoordinateTransform<?>> transforms,
			final Space from, final Space to )
	{
		final List<CoordinateTransform<?>> tList = new ArrayList<>();
		
		// keep track of all input and output axes 
		final HashSet<String> transformInputAxes = new HashSet<>();
		final HashSet<String> transformOutputAxes = new HashSet<>();

		final String[] toAxes = to.getAxisLabels();
		HashSet<String> toAxesRemaining = new HashSet<>();
		toAxesRemaining.addAll( Arrays.asList( toAxes ));

		final String[] fromAxes = from.getAxisLabels();
		HashSet<String> fromAxesRemaining = new HashSet<>();
		fromAxesRemaining.addAll( Arrays.asList( fromAxes ));

		while( !fromAxesRemaining.isEmpty() )
		{
			boolean anyChanged = false;
			for( CoordinateTransform<?> t : transforms )
			{
				String[] tInputs = spaces.getInputAxes(t);
				String[] tOutputs = spaces.getOutputAxes(t);
				
				System.out.println( "t in : " + Arrays.toString(tInputs) );
				System.out.println( "t out: " + Arrays.toString(tOutputs) );
				
				if( tList.contains( t ))
					continue;

				// if 
				if( spaces.outputMatchesAny(t, toAxesRemaining))
				{
					if( AxisUtils.containsAny( transformOutputAxes, tOutputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}

					if( AxisUtils.containsAny( transformInputAxes, tInputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}

					for( String out : tOutputs )
					{
						if( !toAxesRemaining.remove(out) )
							fromAxesRemaining.add(out);
					}
					
					for( String in : tInputs )
					{
						if( !fromAxesRemaining.remove(in) )
							toAxesRemaining.add(in);
					}

					anyChanged = true;
					tList.add(t);

					transformOutputAxes.addAll( Arrays.asList( tOutputs ));
					transformInputAxes.addAll( Arrays.asList( tInputs ));
				}
				System.out.println( " " );
			}

			// if anyChanged = false, it means we
			// iterated through all transformations without making any progress,
			// so no progress can be made, so terminate.
			if( !anyChanged )
				break;
		}
		
		if( !fromAxesRemaining.isEmpty() )
		{
			System.err.println( "uh oh, path to some source axes has not been found");
		}

		final StackedCoordinateTransform totalTransform = new StackedCoordinateTransform(
				from.getName() + " > " + to.getName(), from.getName(), to.getName(), tList);	

		totalTransform.setSpaces(spaces);
		totalTransform.buildTransform();

		return totalTransform;
	}

	public static CoordinateTransform<?> buildTransformFromAxesOLD( 
			Spaces spaces, List<CoordinateTransform<?>> transforms,
			final Space from, final Space to )
	{
		final List<CoordinateTransform<?>> tList = new ArrayList<>();
		final HashSet<String> outAxes = new HashSet<>();
		
		// keep track of all input axes used by 
		final HashSet<String> transformInputAxes = new HashSet<>();

		// keep track of what inputs 
		final HashSet<String> fromAxesRemaining = new HashSet<>();
		fromAxesRemaining.addAll( Arrays.asList(from.getAxisLabels()) ); 

		final String[] outputAxes = to.getAxisLabels();
		HashSet<String> outputAxesRemaining = new HashSet<>();
		outputAxesRemaining.addAll( Arrays.asList( outputAxes ));

		while( !fromAxesRemaining.isEmpty() )
		{
			boolean anyChanged = false;
			for( CoordinateTransform<?> t : transforms )
			{
				String[] tInputs = spaces.getInputAxes(t);
				String[] tOutputs = spaces.getOutputAxes(t);
				
				if( tList.contains( t ))
					continue;

				// if 
				if( spaces.outputMatchesAny(t, outputAxesRemaining))
				{
					if( AxisUtils.containsAny( outAxes, tOutputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}

					if( AxisUtils.containsAny( transformInputAxes, tInputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}
					
					for( String in : tInputs )
					{
						if( !fromAxesRemaining.remove(in) )
							outputAxesRemaining.add(in);
					}

					anyChanged = true;
					tList.add(t);

					outAxes.addAll( Arrays.asList( tOutputs ));

					transformInputAxes.addAll( Arrays.asList( tInputs ));
				}

			}

			// if anyChanged = false, it means we
			// iterated through all transformations without making any progress,
			// so no progress can be made, so terminate.
			if( !anyChanged )
				break;
		}
		
		if( !fromAxesRemaining.isEmpty() )
		{
			System.err.println( "uh oh, path to some source axes has not been found");
		}

		final StackedCoordinateTransform totalTransform = new StackedCoordinateTransform(
				from.getName() + " > " + to.getName(), from.getName(), to.getName(), tList);	

		totalTransform.setSpaces(spaces);
		totalTransform.buildTransform();

		return totalTransform;
	}

}
