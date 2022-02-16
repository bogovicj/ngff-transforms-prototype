import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import org.janelia.saalfeldlab.ngff.SpacesTransforms;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AxisSubspacesExperiments {

	public static void main(String[] args) throws FileNotFoundException {
//		final String testDataF = "/home/john/dev/ngff/ngff-transforms-prototype/src/test/resources/XandYtoXY.json";
		final String testDataF = "/home/john/dev/ngff/ngff-transforms-prototype/src/test/resources/XY.json";

		SpacesTransforms st = readSpacesTransforms(testDataF);
		spacesTest( st );
	}

	public static SpacesTransforms readSpacesTransforms(String testDataF) throws FileNotFoundException {

		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final Gson gson = gsonBuilder.create();
		final SpacesTransforms st = gson.fromJson(new FileReader( testDataF ), SpacesTransforms.class );
		System.out.println( st );
		return st;
	}

	public static void spacesTest( SpacesTransforms st ) {
		Spaces s = new Spaces( st.spaces );

		System.out.println("spaces:");
		s.spaces().forEach(System.out::println);
		System.out.println("");
		System.out.println("axes:");
		s.axes().forEach(System.out::println);
		
		ArrayList<Space> xspaces = s.getSpaces("x");
		System.out.println( xspaces.size());
		System.out.println( xspaces.get(0));
	}

}
