package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.bdv.N5Source;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.multiscales.DatasetTransform;
import org.janelia.saalfeldlab.ngff.multiscales.Multiscale;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;
import org.janelia.saalfeldlab.ngff.vis.Vis;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.Translation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.SubsampleIntervalView;
import net.imglib2.view.Views;


public class MultiscaleExample {

	public static void main(String[] args) throws IOException {

		final String root = args[0];
		final String baseDataset = "/multiscales";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		double r = 128;
		ArrayImg<DoubleType, DoubleArray> img = ArrayImgs.doubles(1028, 1028 );
		Stream<RealPoint> pts = Common.circleSamples( new RealPoint( 256, 256 ), r, r/4 );

		System.out.println( "make base image" );
		Common.render(img, pts.limit(80), r );

		buildMultiscales(img, zarr, baseDataset + "/sample", 3, false);
		buildMultiscales(img, zarr, baseDataset + "/avg", 3, true);
		
		show(zarr, "/multiscales/sample");
		show(zarr, "/multiscales/avg");
	}
	
	public static void show( N5Reader zarr, String d ) throws IOException
	{
		BdvOptions opts = BdvOptions.options().is2D();
		Vis vis = new Vis( zarr ).bdvOptions(opts).space("physical");
		BdvStackSource<?> bdvms = vis.dataset(d).show();
		BdvStackSource<?> bdvs0 = vis.bdvOptions( x -> x.addTo(bdvms)).dataset(d + "/s0").show();
		BdvStackSource<?> bdvs2 = vis.dataset(d + "/s2").show();

		bdvms.setDisplayRangeBounds(0, 255);
		bdvms.setColor(new ARGBType( ARGBType.rgba(255, 128, 128, 255)));

		bdvs0.setDisplayRangeBounds(0, 255);
		bdvs0.setColor(new ARGBType( ARGBType.rgba(128, 255, 128, 255)));

		bdvs2.setDisplayRangeBounds(0, 255);
		bdvs2.setColor(new ARGBType( ARGBType.rgba(128, 128, 255, 255)));

	}

	public static <T extends RealType<T> & NativeType<T>> void buildMultiscales( Img<T> imgBase, N5Writer n5, String base, int nScales, boolean avg ) throws IOException
	{
		final DatasetTransform[] datasets = new DatasetTransform[ nScales ];

		final String spaceName = "physical";
		Space[] spaces = new Space[]{
			Common.makeSpace(spaceName, "space", "um", "x", "y")
		};

		RandomAccessibleInterval<T> img = imgBase;
		final GzipCompression compression = new GzipCompression();

		final int[] blkSize = new int[ imgBase.numDimensions()];
		Arrays.fill(blkSize, 64);

		Scale df = new Scale( 2, 2 );
		Scale factors = new Scale( 1, 1 );
		double sx = 2.2;
		double sy = 3.3;
		Scale s = new Scale( sx, sy ); // s0 to physical

		for (int i = 0; i < nScales; i++) {
			String si = String.format("s%d", i);
			String dset = String.format("%s/%s", base, si);
			System.out.println(dset + " " + Intervals.toString(img));

			N5Utils.save(img, n5, dset, blkSize, compression);
			
			CoordinateTransform<?> t = null;
			if( avg )
			{
				if( i > 0 )
				{
					ScaleCoordinateTransform scale = new ScaleCoordinateTransform("", "", "", s.getScaleCopy());
					double[] xlation = new double[] { sx * ((factors.getScale(0) - 1)/ 2), sy * (factors.getScale(1) - 1)/ 2 };
					TranslationCoordinateTransform xlationct = new TranslationCoordinateTransform("", "", "", xlation);
					System.out.println( "xlation: " + Arrays.toString(xlation));

					t = new SequenceCoordinateTransform(si + "-to-physical", dset, spaceName,
							new RealCoordinateTransform[]{ scale, xlationct});
				}
				else
				{
					t = new ScaleCoordinateTransform(si+"-to-physical", dset, spaceName, s.getScaleCopy());
				}
			}
			else {
				t = new ScaleCoordinateTransform(si+"-to-physical", dset, spaceName, s.getScaleCopy());
			}

			datasets[i] = new DatasetTransform(dset, t );
					
			s.preConcatenate(df);
			factors.preConcatenate(df);
			
			if( spaces != null )
				n5.setAttribute(dset, "coordinateSystems", spaces);

			if( datasets[i] != null )
				n5.setAttribute(dset, "coordinateTransformations", datasets[i].coordinateTransformations );

			if (avg)
				img = Common.downsampleAvg(img, 2);
			else
				img = Common.downsample(img, 2, 2);
		}
		
		Multiscale ms = new Multiscale();
		ms.version = "0.5-prototype";
		ms.name = avg ? "ms_avg" : "ms_sample";
		ms.type = avg ? "averaging" : "sampling";
		ms.spaces = spaces;
		ms.datasets = datasets;

		n5.setAttribute(base, "multiscales", new Multiscale[]{ ms });

	}

}
