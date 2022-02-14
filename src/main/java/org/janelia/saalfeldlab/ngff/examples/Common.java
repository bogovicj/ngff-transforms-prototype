package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.Arrays;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.ngff.axes.Axis;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public class Common {

	public static Space makeSpace( String name, String type, String unit, String... labels)
	{
		return new Space( name, 
				Arrays.stream(labels)
					.map( x -> new Axis(x, type, unit ))
					.toArray( Axis[]::new ));
	}
	
	public static <T extends NativeType<T> & NumericType<T>> RandomAccessibleInterval<T> open( N5Reader n5, String dataset ) throws IOException
	{
		final CachedCellImg<T, ?> imgRaw = (CachedCellImg<T, ?>) N5Utils.open(n5, dataset);
		final RandomAccessibleInterval<T> img;
		if( imgRaw.numDimensions() == 2)
			img = Views.addDimension(imgRaw, 0, 0);
		else
			img = imgRaw;
		
		return img;
	}
	
	public static TransformGraph buildGraph( N5Reader n5 ) throws IOException 
	{
		return buildGraph( n5, "/" );
	}

	public static TransformGraph buildGraph( N5Reader n5, String dataset ) throws IOException 
	{
		Space[] spaces = n5.getAttribute(dataset, "spaces", Space[].class);
		CoordinateTransform[] transforms = n5.getAttribute(dataset, "transformations", CoordinateTransform[].class);
		return new TransformGraph( Arrays.asList( transforms ), Arrays.asList(spaces));	
	}
}
