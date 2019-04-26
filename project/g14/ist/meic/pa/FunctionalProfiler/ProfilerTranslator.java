package ist.meic.pa.FunctionalProfiler;

import java.util.Arrays;

import javassist.*;
import javassist.expr.*;

@NoProfile
public class ProfilerTranslator implements Translator {

	private final String READS_TEMPLATE = "{" +
		" $_ = $proceed();" +
		" ist.meic.pa.FunctionalProfiler.Profiler.getInstance().incReads( $0.getClass().getName() );" +
	"}";

	private final String WRITES_TEMPLATE_FOR_METHOD = "{" +
		" $proceed( $$ );" +
		" ist.meic.pa.FunctionalProfiler.Profiler.getInstance().incWrites( $0.getClass().getName() );" +
	"}";

	private final String WRITES_TEMPLATE_FOR_CONSTRUCTOR = "{" +
		" $proceed( $$ );" +
		" if ( $0 != this ) {" +
		"   ist.meic.pa.FunctionalProfiler.Profiler.getInstance().incWrites( $0.getClass().getName() );" +
		" }" +
	"}";

	@Override
	public void start( ClassPool pool ) throws NotFoundException, CannotCompileException {
		// Nothing...	
	}

	@Override
	public void onLoad( ClassPool pool, String className ) throws NotFoundException, CannotCompileException {
		CtClass ctClass = pool.get( className );
		
		try {
			Object[] annotations = ctClass.getAnnotations();

			if ( !Arrays.stream( annotations ).anyMatch( a -> a instanceof NoProfile ) ) {
				this.profileClass( ctClass );
			}
		} catch ( ClassNotFoundException e ) {
			throw new RuntimeException( e );
		}
	}

	private void profileClass( CtClass ctClass ) throws CannotCompileException, ClassNotFoundException {
		for ( CtMethod ctMethod : ctClass.getDeclaredMethods() ) {
			Object[] annotations = ctMethod.getAnnotations();

			if ( !Arrays.stream( annotations ).anyMatch( a -> a instanceof NoProfile ) ) {
				this.profileMethod( ctMethod );
			}
		}

		for ( CtConstructor ctConstructor : ctClass.getDeclaredConstructors() ) {
			Object[] annotations = ctConstructor.getAnnotations();

			if ( !Arrays.stream( annotations ).anyMatch( a -> a instanceof NoProfile ) ) {
				this.profileConstructor( ctConstructor, ctClass );
			}
		}
	}

	private void profileMethod( CtMethod ctMethod ) throws CannotCompileException {
		ctMethod.instrument( new ExprEditor() {

			@Override
			public void edit( FieldAccess fa ) throws CannotCompileException {
				ProfilerTranslator.this.profileFieldAccess( fa, false );
			}

		} );

		if ( ctMethod.getName().equals( "main" ) ) {
			ctMethod.insertAfter( "{ ist.meic.pa.FunctionalProfiler.Profiler.getInstance().dump(); }" );
		}
	}

	private void profileConstructor( CtConstructor ctConstructor, CtClass ctClass ) throws CannotCompileException {
		ctConstructor.instrument( new ExprEditor() {

			@Override
			public void edit( FieldAccess fa ) throws CannotCompileException {
				ProfilerTranslator.this.profileFieldAccess( fa, true );
			}
			
		} );
	}

	/**
	 * Handles the field access, by adding the respective template.
	 * @param fa The field acces object.
	 * @param isOnConstructor Flag stating if the operations are being computed inside a constructor.
	 */
	private void profileFieldAccess( FieldAccess fa, boolean isOnConstructor ) throws CannotCompileException {
		if ( fa.isStatic() ) {
			// Skip static fields, because they do not belong to objects;
			// they belong to classes.
			return;
		}

		if ( fa.isReader() ) {
			fa.replace( this.READS_TEMPLATE );
		}

		if ( fa.isWriter() ) {
			fa.replace( isOnConstructor
				? this.WRITES_TEMPLATE_FOR_CONSTRUCTOR
				: this.WRITES_TEMPLATE_FOR_METHOD
			);
		}
	}
}

