package org.janelia.saalfeldlab.ngff.transforms;

import static org.junit.Assert.assertArrayEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.examples.Common;
import org.janelia.saalfeldlab.ngff.spaces.ArraySpace;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.junit.Test;

import net.imglib2.RealPoint;

public class SubspaceTransformsTests {
	
//	@Test
//	public void testTransformInferenceMultilevel()
//	{
//		
//	}

	@Test
	public void testCoordinateTransformSubspaceMultilevel()
	{
		Space in = new ArraySpace(5);
		Space a = Common.makeSpace("a space", "arbitrary", "none", "a");
		Space out = new Space( "xyczt", AxisUtils.buildAxes( "x", "y", "c", "z", "t" ));
		Spaces spaces = new Spaces(Stream.of(in, out, a));

		ScaleCoordinateTransform scaleXYZ = new ScaleCoordinateTransform(
				"xyz",  new String[]{"dim_0", "dim_1", "dim_3" }, new String[] { "x", "y", "z" }, new double[] { 3, 4, 5 });

		ScaleCoordinateTransform scale4a = new ScaleCoordinateTransform(
				"t",  new String[]{"dim_4"}, new String[] { "a" }, new double[] { 2 });

		TranslationCoordinateTransform scaleT = new TranslationCoordinateTransform(
				"t",  new String[]{"a"}, new String[] { "t" }, new double[] { 0.5 });

		IdentityCoordinateTransform idC = new IdentityCoordinateTransform(
				"c",  new String[]{"dim_2"}, new String[] { "c" } );

		spaces.updateTransforms( Stream.of( scaleXYZ, scale4a, scaleT, idC ));

		RealCoordinate pt = new RealCoordinate( new RealPoint( 1.0, 10.0, 100.0, 1000.0, 10000.0 ), in);
		RealCoordinate tmp1 = new RealCoordinate( 5 );
		RealCoordinate tmp2 = new RealCoordinate( 5 );
		RealCoordinate tmp3 = new RealCoordinate( 5 );
		RealCoordinate res = new RealCoordinate( 5 );

		scale4a.apply(pt, tmp1);
		System.out.println( tmp1 );
		System.out.println( tmp1.getSpace() );

		scaleXYZ.apply(tmp1, tmp2);
		System.out.println( tmp2 );
		System.out.println( tmp2.getSpace() );

		idC.apply(tmp2, tmp3);
		System.out.println( tmp3 );
		System.out.println( tmp3.getSpace() );

		scaleT.apply(tmp3, res);
		System.out.println( res );
		System.out.println( res.getSpace() );

	}

//	@Test
//	public void testCoordinateTransformSubspace5()
//	{
//		Space in = new ArraySpace(5);
//		Space out = new Space( "xyczt", AxisUtils.buildAxes( "x", "y", "c", "z", "t" ));
//
//		Spaces spaces = new Spaces(Stream.of(in, out).collect(Collectors.toList()));
//
//		ScaleCoordinateTransform scaleXYZ = new ScaleCoordinateTransform(
//				"013>xyz",  new String[]{"dim_0", "dim_1", "dim_3" }, new String[] { "x", "y", "z" }, new double[] { 3, 4, 5 });
//
//		ScaleCoordinateTransform scaleT = new ScaleCoordinateTransform(
//				"4<t",  new String[]{"dim_4"}, new String[] { "t" }, new double[] { 2 });
//
//		IdentityCoordinateTransform idC = new IdentityCoordinateTransform(
//				"2>c",  new String[]{"dim_2"}, new String[] { "c" } );
//
//		spaces.updateTransforms( Stream.of( scaleXYZ, scaleT, idC ));
//
//		RealCoordinate pt = new RealCoordinate( new RealPoint( 1, 1, 1, 1, 1 ), in);
//		RealCoordinate tmp1 = new RealCoordinate( 5 );
//		RealCoordinate tmp2 = new RealCoordinate( 5 );
//
//		scaleXYZ.apply(pt, tmp1);
//		System.out.println( tmp1 );
//		System.out.println( tmp1.getSpace() );
//
//		scaleT.apply(tmp1, tmp2);
//		System.out.println( tmp2 );
//		System.out.println( tmp2.getSpace() );
//
//	}
//
//	@Test
//	public void testCoordinateTransformSubspace2()
//	{
//		Space in = Common.makeSpace("in", "space", "um", "0", "1");
//		Space out = Common.makeSpace("out", "space", "um", "x", "y");
//		
//		ScaleCoordinateTransform scaleXY = new ScaleCoordinateTransform(
//				"sx",  new String[]{"0", "1"}, new String[] { "x", "y" }, new double[] { 2, 3 });
//		scaleXY.setInputSpace(in);
//		scaleXY.setOutputSpace(out);
//
//		ScaleCoordinateTransform scaleX = new ScaleCoordinateTransform(
//				"sx",  new String[]{"0"}, new String[] { "x" }, new double[] { 2 });
//		scaleX.setInputSpace(in.subSpace("", "0"));
//		scaleX.setOutputSpace(out.subSpace("", "x"));
//
//		ScaleCoordinateTransform scaleY = new ScaleCoordinateTransform(
//				"sy",  new String[]{"1"}, new String[] { "y" }, new double[] { 3 });
//		scaleY.setInputSpace(in.subSpace("", "1"));
//		scaleY.setOutputSpace(out.subSpace("", "y"));
//
//		RealCoordinate pt = new RealCoordinate( new RealPoint(1, 1 ), in);
//		RealCoordinate tmp = new RealCoordinate( 2 );
//		RealCoordinate res = new RealCoordinate( 2 );
//
//		scaleXY.apply(pt, res);
//		assertArrayEquals( new double[]{ 2, 3 }, res.positionAsDoubleArray(), 1e-9 );
//		System.out.println( "" );
//
//		scaleX.apply(pt, tmp);
//		System.out.println( tmp );
//		System.out.println( tmp.getSpace() );
//		assertArrayEquals( new double[]{ 2, 1 }, tmp.positionAsDoubleArray(), 1e-9 );
//
//		scaleY.apply(tmp, res);
//		System.out.println( res );
//		System.out.println( res.getSpace() );
//		assertArrayEquals( new double[]{ 3, 2 }, res.positionAsDoubleArray(), 1e-9 );
//	}

}
