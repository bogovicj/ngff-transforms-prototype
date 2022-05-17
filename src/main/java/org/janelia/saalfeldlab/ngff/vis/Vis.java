package org.janelia.saalfeldlab.ngff.vis;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.bdv.N5Source;
import org.janelia.saalfeldlab.n5.ij.N5Factory;
import org.janelia.saalfeldlab.ngff.examples.Common;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.multiscales.Multiscale;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.BoundingBoxEstimation;
import net.imglib2.realtransform.BoundingBoxEstimation.Method;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class Vis {

	N5Reader n5;
	
	String dataset;

	String space;

	TransformGraph graph;

	BdvOptions options;
	
	public Vis() { }

	public Vis( N5Reader n5 ) { 
		n5( n5 );
	}
	
	public Vis n5( N5Reader n5 ) {
		this.n5 = n5;
		return this;
	}
	
	public N5Reader getN5()
	{
		return n5;
	}
	
	public String getDataset()
	{
		return dataset;
	}

	public TransformGraph getGraph()
	{
		return graph;
	}

	public Vis n5( String n5root ) {
		try {
			return n5( new N5Factory().openReader( n5root ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public Vis bdvOptions( BdvOptions options ) {
		this.options = options;
		return this;
	}
	
	public Vis bdvOptions( Consumer<BdvOptions> f )
	{
		if( options != null )
			f.accept( options );

		return this;
	}

	public Vis dataset( String dataset ) {
		this.dataset = dataset;
		addTransforms(dataset);
		return this;
	}
	
	public Vis space( String space ) {
		this.space = space;
		return this;
	}

	public Vis transforms( TransformGraph g ) {
		this.graph = g;
		return this;
	}

	public Vis addTransforms( TransformGraph g ) {
		if( graph == null )
			graph = g;
		else
			graph.add(g);
		return this;
	}

	public Vis transforms( String gPath ) {
		try {
			return transforms( Common.buildGraph(n5, gPath ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public Vis addTransforms( String gPath ) {
		try {
			return addTransforms( Common.buildGraph(n5, gPath ));
		} catch (IOException e) {
//			e.printStackTrace();
		}
		return this;
	}
	
	public <T extends NativeType<T> & NumericType<T>> BdvStackSource<?> show() {

		BdvOptions opts = Optional.ofNullable(options).orElse(BdvOptions.options());
		if( options == null )
			options = opts;

		String thisSpace = Optional.ofNullable(space).orElse(dataset);
		
//		System.out.println( "\nshow with");
//		System.out.println( "  datset: " + dataset );
//		System.out.println( "  space: " + thisSpace );
		
		Multiscale[] multiscales = null;
		try {
			multiscales = n5.getAttribute(dataset, "multiscales", Multiscale[].class );
			if( multiscales != null )
			{
				N5Source<T> src = (N5Source<T>) Common.openMultiscaleSource(n5, dataset);
				return BdvFunctions.show(src);
			}
		} catch (IOException e) { }

		RandomAccessibleInterval<T> img;
		try {
			img = Common.open(n5, dataset);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		

		// try with a fwd affine
//		TransformPath pa = graph.path(dataset, thisSpace).get();
		Optional<AffineTransform3D> axfm = graph.path(dataset, thisSpace).map( p -> p.totalAffine3D(n5));
		if( axfm.isPresent() ) {
//			System.out.println( "showing with affine: " + axfm.get());
			return BdvFunctions.show( new RandomAccessibleIntervalSource<>(
					img, Util.getTypeFromInterval(img), axfm.get(), dataset + " - " + space ), opts );
		}

		// try with an inverse transform
		Optional<RealTransform> xfm = graph.path(thisSpace, dataset ).map( p -> p.totalTransform(n5));
		if( xfm.isPresent())
		{
			final RealTransform transform = xfm.get();
//			System.out.println( "showing with transform: " + transform);
			final Interval transformedInterval = new BoundingBoxEstimation( Method.FACES, 5 )
					.estimatePixelInterval(transform, img);

			RealRandomAccessible<T> rra = Views.interpolate( Views.extendZero( img ), new NLinearInterpolatorFactory<T>());
			RealRandomAccessible<T>	transformedRra = new RealTransformRandomAccessible<>(rra, transform);
			return BdvFunctions.show(transformedRra , transformedInterval, dataset + " - " + thisSpace, opts );
		}
		
//		System.out.println( "uh oh");
		return null;
	}

}
