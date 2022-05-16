package net.imglib2;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.SubsetRealTransform;

public class SubsetRealTransformTests
{
	
	@Test
	public void testSingleComponent()
	{
		AffineTransform3D a = new AffineTransform3D();
		a.scale( 2, 3, 4 );

		SubsetRealTransform t0 = new SubsetRealTransform( a, new int[] {1, 2, 3 });
		RealPoint p = new RealPoint( 1.0, 1.0, 1.0, 1.0 );
		t0.apply( p, p );
		assertArrayEquals( new double[] {1, 2, 3, 4}, p.positionAsDoubleArray(), 1e-9 );
		
		SubsetRealTransform t1 = new SubsetRealTransform( a, new int[] {0, 2, 3 });
		p = new RealPoint( 1.0, 1.0, 1.0, 1.0 );
		t1.apply( p, p );
		assertArrayEquals( new double[] {2, 1, 3, 4}, p.positionAsDoubleArray(), 1e-9 );

		SubsetRealTransform t2 = new SubsetRealTransform( a, new int[] {0, 1, 3 });
		p = new RealPoint( 1.0, 1.0, 1.0, 1.0 );
		t2.apply( p, p );
		assertArrayEquals( new double[] {2, 3, 1, 4}, p.positionAsDoubleArray(), 1e-9 );

		SubsetRealTransform t3 = new SubsetRealTransform( a, new int[] {0, 1, 2 });
		p = new RealPoint( 1.0, 1.0, 1.0, 1.0 );
		t3.apply( p, p );
		assertArrayEquals( new double[] {2, 3, 4, 1}, p.positionAsDoubleArray(), 1e-9 );
	}
	
	@Test
	public void testMultiComponent()
	{
		AffineTransform3D a = new AffineTransform3D();
		a.scale( 2, 3, 4 );
		
		// p = (1, 10, 100, 1000)
		// t(p) = (20, 300, 4000 )
		// q = 300 

		final SubsetRealTransform t = new SubsetRealTransform( a, new int[] {1, 2, 3 }, new int[]{2, 4, 0});
		final RealPoint p = new RealPoint( 1.0, 10.0, 100.0, 1000.0 ); // 
		final RealPoint q = new RealPoint( 5 );
		t.apply( p, q );

		assertArrayEquals( new double[]{4000, 0, 20, 0, 300}, q.positionAsDoubleArray(), 1e-9 );
		
		
		final SubsetRealTransform t2 = new SubsetRealTransform( a, new int[] {1, 2, 3 }, new int[]{-1, 0, -1});
		final RealPoint q2 = new RealPoint( 1 );
		t2.apply( p, q2 );

		assertArrayEquals( new double[]{300}, q2.positionAsDoubleArray(), 1e-9 );
	}

}
