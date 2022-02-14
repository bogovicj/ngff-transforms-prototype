
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.PositionFieldTransform;
import net.imglib2.realtransform.RealTransformRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.StackedRealTransform;
import net.imglib2.realtransform.inverse.InverseRealTransformGradientDescent;
import net.imglib2.realtransform.inverse.RealTransformFiniteDerivatives;
import net.imglib2.realtransform.inverse.RegularizedDifferentiableRealTransform;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class PositionFieldInverseTest {

	public static void main(String[] args) {
		
		ArrayImg<DoubleType, DoubleArray> pfimg = ArrayImgs.doubles(
				new double[]{0, 5, 6, 10, 11, 15, 16, 20, 21, 25}, 
				10);
		
		RealRandomAccessible<DoubleType> pf = Views.interpolate( Views.extendValue(pfimg, 30), 
				new NLinearInterpolatorFactory<>());

		PositionFieldTransform pfx = new PositionFieldTransform<>(pf);
		WrappedIterativeInvertibleRealTransform pfxi = new WrappedIterativeInvertibleRealTransform( pfx );

		RandomAccessibleInterval<DoubleType> pfImgInv = invertPositionField1d( pfimg, new ArrayImgFactory<DoubleType>( new DoubleType() ) );
		
		PositionFieldTransform pfi = new PositionFieldTransform<>(
				Views.interpolate( Views.extendBorder(pfImgInv), 
					new NLinearInterpolatorFactory<>()));

	}
	
	public static <T extends RealType<T>> Pair<T,T> minMax(
			final RandomAccessibleInterval<T> pfieldImg ) {

		T min = null;
		T max = null;
		
		Cursor<T> c = Views.flatIterable(pfieldImg).cursor();
		while( c.hasNext())
		{
			c.fwd();
			if( min == null )
			{
				min = c.get().copy();
				max = c.get().copy();
			}
			else
			{
				T v = c.get();
				if( v.compareTo(min) < 0)
					min.set(v);

				if( v.compareTo(max) > 0)
					max.set(v);
			}
		}

		System.out.println( "min: " + min );
		System.out.println( "max: " + max );

		return new ValuePair<>( min, max );
	}
	
	public static <T extends RealType<T>, S extends RealType<S>> RandomAccessibleInterval<S> invertPositionField1d( 
			final RandomAccessibleInterval<T> pfieldImg, ImgFactory<S> factory ) {

		Pair<T, T> minMax = minMax(pfieldImg);
		T min = minMax.getA();
		T max = minMax.getB();
		double initPos = (max.getRealDouble() - min.getRealDouble())/2;
		
		Interval itvl = Intervals.createMinMax(
				(long)Math.floor(min.getRealDouble()),
				(long)Math.ceil(max.getRealDouble()));
		System.out.println( "itvl sz: " + Intervals.toString(itvl));


		Img<S> outRaw = factory.create( itvl );
		System.out.println( "outRaw sz: " + Intervals.toString(outRaw));

		IntervalView<S> out = Views.interval( outRaw, itvl );
		System.out.println( "out sz: " + Intervals.toString(out));

		RealRandomAccessible<T> pf = Views.interpolate( Views.extendValue(pfieldImg, 30), 
				new NLinearInterpolatorFactory<>());

		PositionFieldTransform<T> pfx = new PositionFieldTransform<>(pf);
		RealTransformFiniteDerivatives dt = new RealTransformFiniteDerivatives( pfx );
		dt.setStep(0.05);
		InverseRealTransformGradientDescent inverseTransform = new InverseRealTransformGradientDescent( 1, dt );
		inverseTransform.setBeta(0.6);
		inverseTransform.setStepSizeMaxTries(10);
		inverseTransform.setStepSize(0.05);

		inverseTransform.setTolerance(0.01);
		inverseTransform.setMaxIters(2000);
		
		System.out.println(" ");

//		RealPoint init = new RealPoint(1);
		RealPoint qpt = new RealPoint(1);
		
		double[] init = new double[1];
		double[] q = new double[1];
		double[] x = new double[1];

		final Cursor<S> c = out.cursor();
		while( c.hasNext())
		{
			c.fwd();

//			inverseTransform.apply(c, qpt);
//			c.get().setReal( qpt.getDoublePosition(0) );

			c.localize(x);
			init[0] = 0;
			inverseTransform.inverseTol(x, init, 0.01, 1000);
			c.get().setReal( inverseTransform.getEstimate()[0] );

		}

		return out;
	}
	
	public static void backups()
	{
//		RealPoint p = new RealPoint( 1 );
//		RealPoint q = new RealPoint( 1 );
//		RealPoint qi = new RealPoint( 1 );
//		p.setPosition(10, 0);
//		
//		pfx.apply(p, q);
//		System.out.println( q );

//		q.setPosition(2.0, 0);
//		pfxi.applyInverse(p2, q);
//		System.out.println( p2 );

//		minMax( pfimg );



//		RealTransformFiniteDerivatives dt = new RealTransformFiniteDerivatives( pfxi );
//		dt.setStep(0.05);
//		InverseRealTransformGradientDescent inverseTransform = new InverseRealTransformGradientDescent( 1, dt );
//		inverseTransform.setStepSize(0.01);
//
////		RegularizedDifferentiableRealTransform regDt = new RegularizedDifferentiableRealTransform( dt, 0.05);
////		InverseRealTransformGradientDescent inverseTransform = new InverseRealTransformGradientDescent( 1, regDt );
//
//		double[] x = new double[] { 13 };
//		double[] xi = new double[] { 1 };
//		inverseTransform.inverseTol(x, xi, 0.05, 1000);
//		System.out.println( Arrays.toString( inverseTransform.getEstimate()));


//		q.setPosition(5, 0);
//		pfxi.applyInverse(qi, q);
//		System.out.println( qi );
		
	}

}
