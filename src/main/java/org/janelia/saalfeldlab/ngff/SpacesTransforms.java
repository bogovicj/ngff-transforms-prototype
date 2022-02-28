package org.janelia.saalfeldlab.ngff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.ArraySpace;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SpacesTransforms {

	public Space[] spaces;

	public CoordinateTransform[] transforms;
	
	public SpacesTransforms( Space[] spaces, CoordinateTransform[] transforms)
	{
		this.spaces = spaces;
		this.transforms = transforms;
	}

	public static SpacesTransforms load( Reader reader )
	{
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final Gson gson = gsonBuilder.create();
		final SpacesTransforms st = gson.fromJson(reader, SpacesTransforms.class );	

		return st;
	}

	public static SpacesTransforms load( File f ) throws FileNotFoundException {
		return load( new FileReader( f.getAbsolutePath() ));
	}
	
	public static SpacesTransforms loadFile( String path ) throws FileNotFoundException {
		return load( new FileReader( path ));
	}

	public Spaces buildSpaces() {
		return buildSpaces( 0 );
	}
	
	public Spaces buildSpaces( int nd ) {
		Spaces s = new Spaces( spaces );
		if( nd > 0 )
			s.add(new ArraySpace(nd));
		
		return s;
	}

	public TransformGraph buildTransformGraph() {
		return buildTransformGraph(0);
	}

	public TransformGraph buildTransformGraph( int nd ) {
		return new TransformGraph( Arrays.asList(transforms), buildSpaces( nd ));
	}

}
