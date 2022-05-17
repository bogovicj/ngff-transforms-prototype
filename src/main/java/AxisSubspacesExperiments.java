import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.ngff.SpacesTransforms;
import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.examples.Common;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.ArraySpace;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.MatrixCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.RealTransformCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.StackedCoordinateTransform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.componenttree.BuildComponentTree;
import net.imglib2.realtransform.RealComponentMappingTransform;
import net.imglib2.realtransform.RealInvertibleComponentMappingTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.Scale2D;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.SubsetRealTransform;
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
//		final String testDataF = "src/test/resources/multiLevelAxisGraph.json";
//		final String testDataF = "src/test/resources/multiLevelAxisGraph2.json";
		final String testDataF = "src/test/resources/transformSeqSimple.json";
//		final String testDataF = "src/test/resources/invalidAxisGraph.json";

		SpacesTransforms st = SpacesTransforms.loadFile(testDataF);
		System.out.println( st );

//		TransformGraph tgraph = st.buildTransformGraph( 2 );
//		transformsTest( tgraph );

//		stackedTest( st );

//		stackedSubspaceTest( st );

//		stackedTransformTest();

//		permutationTest();
		
//		subspaceNoStackTest( st );
		
//		permMtxTest();

//		multilevelAxisGraphTest( st, "xyczt" );

		// run with multiLevelAxisGraph2
//		multilevelAxisGraphTest( st, "z" );

		// run with transformSeqSimple
		multilevelAxisGraphTest( st, "ab", 3 );

		
		// run with invalidAxisGraph
//		System.out.println( isValid( st.transforms ));
	}
	
	public static void permMtxTest()
	{
		String[] from = new String[] { "0", "1", "x", "2", "y", "3" };
		String[] to = new String[] { "x", "y" };
		
		/*
		 * p should be [2,4]
		 * 
		 * 
		 * permMatrix should be
		 * [ 0, 0, 1, 0, 0, 0, 
		 *   0, 0, 0, 0, 1, 0 ] 
		 *   
		 *   the elements that should equal 1 are (0,2) and (1,4)
		 *   at indices (2) and 6*1 + 4 = 10
		 */
		
		final int[] p = AxisUtils.findPermutation( from, to );
		final double[] permMatrix = AxisUtils.matrixFromPermutation(p, from.length );
		MatrixCoordinateTransform ct = new MatrixCoordinateTransform(
				"", from, to, permMatrix, 6, 2);

		Space fromSpace = Common.makeSpace("from", "", "", from );
		Space toSpace = Common.makeSpace("to", "", "", to );

		ct.setInputSpace(fromSpace);
		ct.setOutputSpace(toSpace);

		RealCoordinate f = new RealCoordinate(6, fromSpace);
		f.setPosition(new double[] {1, 2, 3, 4, 5, 6});

		RealCoordinate t = new RealCoordinate(2, toSpace);
		ct.apply(f, t);

		System.out.println( f );
		System.out.println( t );
	}

	public static void multilevelAxisGraphTest( SpacesTransforms st , String tgtSpaceName )
	{
		multilevelAxisGraphTest( st, tgtSpaceName, 5 );
	}

	public static void multilevelAxisGraphTest( SpacesTransforms st , String tgtSpaceName, int nd )
	{
		System.out.println( "multilevelAxisGraphTest" );
		System.out.println( st );

		TransformGraph g = st.buildTransformGraph(nd);
		g.updateTransforms();
//		g.getSpaces().updateTransforms( g.getTransforms().stream() );
		System.out.println( g );
		System.out.println( g.getTransforms().size() );
//		g.getTransforms().stream().forEach( System.out::println );

//		CoordinateTransform<?> t = g.getTransforms().stream().filter(x -> x.getName().equals("0>ab")).findFirst().get();
//		System.out.println( t );

//		Space space0 = Common.makeSpace("", "0", "", "dim_0");
//		RealCoordinate p = new RealCoordinate( 1, space0 );
//		p.setPosition(new double[]{1.0});
//		RealCoordinate q = new RealCoordinate( 2 );
//		t.apply(p, q);
//		System.out.println( p );
//		System.out.println( q );


		Space arraySpace = g.getSpaces().getSpace("");
		Space tgtSpace = g.getSpaces().getSpace( tgtSpaceName );

		SequenceCoordinateTransform xfm = buildTransformFromAxes(
				g.getSpaces(), Arrays.asList(st.transforms),
				arraySpace, tgtSpace );

		System.out.println( xfm );

		System.out.println( "" );
		System.out.println( "tform list:" );
		Arrays.stream(xfm.getTransformations()).forEach( System.out::println );
		System.out.println( "" );

//		System.out.println( "" );
//		cumNeededAxes( xfm.getTransformations() );
//		cumInputAxes( xfm.getTransformations() );
//		System.out.println( "" );

//		System.out.println( "" );
//		ArrayList< String[] > axOrders = axisOrdersForTransform( xfm.getTransformations(), tgtSpace.getAxisLabels() );
//		for( String[] axes : axOrders )
//			System.out.println(String.join(" ", axes ));
//
//		System.out.println( "" );
//		
//		ArrayList< int[] > inIdxes = inputIndexesFromAxisOrders( xfm.getTransformations(), axOrders );
//		for( int[] idxs : inIdxes )
//			System.out.println( Arrays.toString( idxs ));
//
//		System.out.println( "" );
//
//		ArrayList< int[] > outIdxes = outputIndexesFromAxisOrders( xfm.getTransformations(), axOrders );
//		for( int[] idxs : outIdxes )
//			System.out.println( Arrays.toString( idxs ));
//
//		RealTransformSequence totalTransform = new RealTransformSequence();
//		CoordinateTransform[] tforms = xfm.getTransformations();
//		for( int i = 0; i < tforms.length; i++ )
//		{
//			totalTransform.add( new SubsetRealTransform( tforms[i].getTransform(), inIdxes.get( i ), outIdxes.get(i) ));
//		}
//		System.out.println( "" );
		
		RealTransformSequence totalTransform = xfm.getTransform();
		
		System.out.println( "tform src dims: " + totalTransform.numSourceDimensions());
		System.out.println( "tform tgt dims: " + totalTransform.numTargetDimensions());

//		RealPoint p = new RealPoint( 1.0, 1.0, 1.0, 1.0, 1.0 );
//		RealPoint q = new RealPoint( 1 );
//		totalTransform.apply( p, q );
//		System.out.println( "" );
//		System.out.println( q );
		
		RealPoint p = new RealPoint( 1.0, 1.0, 1.0 );
		RealPoint q = new RealPoint( 2 );
		totalTransform.apply( p, q );
		System.out.println( "" );
		System.out.println( q );
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
//		for( Coordinate)Transform ct : subTransforms )
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
	
	public static SequenceCoordinateTransform buildTransformFromAxes( 
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

		System.out.println( "fromAxesRemaining: " + fromAxesRemaining );
		System.out.println( "toAxesRemaining: " + toAxesRemaining );

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

					System.out.println( "fromAxesRemaining: " + fromAxesRemaining );
					System.out.println( "toAxesRemaining: " + toAxesRemaining );
					System.out.println( "" );

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
		
		// we used to need stacked coordinate transform now we can do with a sequence
//		final StackedCoordinateTransform totalTransform = new StackedCoordinateTransform(
//				from.getName() + " > " + to.getName(), from.getName(), to.getName(), tList);	
//		totalTransform.setSpaces(spaces);
//		totalTransform.buildTransform();

		// need to reverse the list because
		// we build it from output to input
		Collections.reverse(tList);
		final SequenceCoordinateTransform totalTransform = new SequenceCoordinateTransform( "total", from.getName(), to.getName(), tList );
		totalTransform.setInputSpace(from);
		totalTransform.setOutputSpace(to);
//		spaces.updateTransforms( Stream.of( totalTransform ) );


		RealCoordinate tmpPtSrc = new RealCoordinate( from.numDimensions(), from );
		tmpPtSrc.positionToIndexes();
		RealCoordinate tmpPtDst = totalTransform.applyAppend( tmpPtSrc );
		System.out.println( "dst : " + tmpPtDst.getSpace() );
		
		if( to.isSubspaceOf( tmpPtDst.getSpace() ))
		{
			// TODO finish this
			final int[] p = AxisUtils.findPermutation( tmpPtDst.getSpace().getAxisLabels(), to.getAxisLabels());
			final double[] permMatrix = AxisUtils.matrixFromPermutation(p, tmpPtDst.getSpace().numDimensions());

			return totalTransform;
		}
		else
		{
			System.err.println("theres a problem");
			return null;
		}

//		if( to.axesLabelsMatch( tmpPtDst.getSpace().getAxisLabels() ))
//			return totalTransform;
//		else if ( to.axesEquals( tmpPtDst.getSpace() ) )
//		{
//			String[] inAxisLabels = tmpPtDst.getSpace().getAxisLabels();
//			String[] outAxisLabels = to.getAxisLabels();
//			// go from the transforms output to the output axis order
//			final int[] outPermParams = AxisUtils.findPermutation( inAxisLabels, outAxisLabels );
//
//			RealComponentMappingTransform perm = new RealComponentMappingTransform( outPermParams.length, outPermParams );
//			RealTransformCoordinateTransform< RealComponentMappingTransform > post = new RealTransformCoordinateTransform< RealComponentMappingTransform >( "", inAxisLabels, outAxisLabels, perm );
//			spaces.updateTransforms( Stream.of( post ) );
//
//			tList.add( post );
//			final SequenceCoordinateTransform totalTransformPerm = new SequenceCoordinateTransform( "total", "from", "to", tList );
//			spaces.updateTransforms( Stream.of( totalTransformPerm ) );
//
//			totalTransformPerm.apply( tmpPtSrc, tmpPtDst );
//			System.out.println( "dst after perm: " + tmpPtDst.getSpace() );
//
//			return totalTransformPerm;
//		}
//		else
//		{
//			System.err.println("theres a problem");
//			return null;
//		}

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

	public static void tformGraph( final CoordinateTransform<?>[] tforms )
	{

		
	}

	public static void cumNeededAxes( final CoordinateTransform<?>[] tforms )
	{
		System.out.println("cumNeededAxes");
		ArrayList<CoordinateTransform> tlist = new ArrayList<>();
		Collections.addAll( tlist, tforms );
		Collections.reverse( tlist );

		ArrayList<HashSet<String>> cAxisList = new ArrayList<>();
		HashSet<String> prev = null;
		for( CoordinateTransform t : tlist )
		{
			System.out.println( "t: " + t );
			System.out.println( "  axes : " + Arrays.toString( t.getInputAxes() ) );
			HashSet< String > set = new HashSet<>();
			Collections.addAll( set, t.getInputAxes());
			if( prev != null )
				set.addAll( prev );

			prev = set;
			cAxisList.add( set );
		}

//		System.out.println( " ");
//		for( HashSet<String> s : cAxisList )
//			System.out.println(String.join(" ", s) );
//
//		Collections.reverse( cAxisList );
//
//		System.out.println( " ");
//		for( HashSet<String> s : cAxisList )
//			System.out.println(String.join(" ", s) );
		
	}

	public static void cumInputAxes( final CoordinateTransform<?>[] tforms )
	{
		System.out.println("cumInputAxes");
		ArrayList<CoordinateTransform<?>> tlist = new ArrayList<>();
		Collections.addAll( tlist, tforms );
		Collections.reverse( tlist );

		ArrayList<HashSet<String>> cAxisList = new ArrayList<>();
		HashSet<String> prev = null;
		for( CoordinateTransform<?> t : tlist )
		{
			System.out.println( "t: " + t );
			System.out.println( "  axes : " + Arrays.toString( t.getInputAxes() ) );
			HashSet< String > set = new HashSet<>();
			Collections.addAll( set, t.getInputAxes());
			if( prev != null )
				set.addAll( prev );

			prev = set;
			cAxisList.add( set );
		}

		System.out.println( " ");
		for( HashSet<String> s : cAxisList )
			System.out.println(String.join(" ", s) );

		Collections.reverse( cAxisList );

		System.out.println( " ");
		for( HashSet<String> s : cAxisList )
			System.out.println(String.join(" ", s) );
	}
	
	/**
	 * Checks if the list of transformations is valid.
	 * A list is valid if an axis is an output of only one transformation,
	 * and there are no loops in the graph.
	 * 
	 * @param tforms transformation list
	 * @return if the transform is valie
	 */
	public static boolean isValid( CoordinateTransform<?>[] tforms )
	{
		HashSet<String> outputAxes = new HashSet<>();	
		for( CoordinateTransform<?> t : tforms )
		{
			for( String a : t.getOutputAxes() )
				if( outputAxes.contains( a ))
					return false;
				else
					outputAxes.add( a );
		}
		return true;
	}
	
	public static ArrayList< String[] > axisOrdersForTransform( final CoordinateTransform<?>[] tforms, final String[] tgtAxes )
	{
		final ArrayList<String[]> axisOrders = new ArrayList<>();
		axisOrders.add( tgtAxes );

		final ArrayList<CoordinateTransform<?>> tlist = new ArrayList<>();
		Collections.addAll( tlist, tforms );
		Collections.reverse( tlist );

		final ArrayList<String> axes = new ArrayList<>();
		Collections.addAll( axes, tgtAxes );
		for( final CoordinateTransform<?> t : tlist )
		{
			final List< String > inAx = Arrays.asList( t.getInputAxes() );
			final List< String > outAx = Arrays.asList( t.getOutputAxes() );
			final int i = firstIndex( axes, outAx );
			axes.removeAll( outAx );
			axes.addAll( i < 0 ? 0 : i, inAx );

			axisOrders.add( axes.stream().toArray( String[]::new ));
		}
		Collections.reverse( axisOrders );
		return axisOrders;
	}

	private static <T> int firstIndex( List<T> list, List<T> search )
	{
		int idx = -1;
		for( T t : search )
		{
			final int i = list.indexOf( t );
			if( i >= 0 && (idx < 0 || i < idx ))
				idx = i;
		}
		return idx;
	}

	public static ArrayList< int[] > inputIndexesFromAxisOrders( final CoordinateTransform<?>[] tforms, List<String[]> axisOrders )
	{
		final ArrayList< int[] > idxList = new ArrayList<>();
		for( int i = 0; i < tforms.length; i++ )
			idxList.add( indexes( tforms[i].getInputAxes(), axisOrders.get( i )));

		return idxList;
	}
	
	public static ArrayList< int[] > outputIndexesFromAxisOrders( final CoordinateTransform<?>[] tforms, List<String[]> axisOrders )
	{
		final ArrayList< int[] > idxList = new ArrayList<>();
		for( int i = 0; i < tforms.length; i++ )
			idxList.add( indexes( tforms[i].getOutputAxes(), axisOrders.get( i + 1 )));

		return idxList;
	}
	
	/**
	 * Returns the indexes of src objects into tgt object.
	 * 
	 * 
	 * @param <T> the type
	 * @param src
	 * @param tgt
	 * @return
	 */
	private static <T> int[] indexes( T[] src, T[] tgt )
	{
		int[] idxs = new int[ src.length ];
		for( int i = 0; i < src.length; i++ )
			idxs[ i ] = indexOf( src[i], tgt );

		return idxs;
	}

	private static <T> int indexOf( T t, T[] tgt )
	{
		for( int i = 0; i < tgt.length; i++ )
			if( tgt[i].equals( t ))
				return i;

		return -1;
	}

}
