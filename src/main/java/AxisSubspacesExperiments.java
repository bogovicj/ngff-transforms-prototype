import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
		final String testDataF = "src/test/resources/stack.json";

		SpacesTransforms st = SpacesTransforms.loadFile(testDataF);
		System.out.println( st );

//		TransformGraph tgraph = st.buildTransformGraph( 2 );
//		transformsTest( tgraph );

//		stackedTest( st );

		stackedSubspaceTest( st );

//		stackedTransformTest();

//		permutationTest();
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

}
