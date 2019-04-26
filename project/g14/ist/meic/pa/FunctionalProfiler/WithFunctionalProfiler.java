package ist.meic.pa.FunctionalProfiler;

import java.util.Arrays;

import java.io.IOException;

import javassist.*;

@NoProfile
public class WithFunctionalProfiler {

	public static void main( String[] args ) throws NotFoundException, CannotCompileException, IOException, Throwable {
		if ( args.length < 1 ) {
			System.err.println( "Arguments must contain at least a program name!" );

			return;
		}

		final String programName = args[ 0 ];

		final String[] programArgs = Arrays.copyOfRange( args, 1, args.length );

		Translator translator = new ProfilerTranslator();

		ClassPool pool = ClassPool.getDefault();

		Loader classLoader = new Loader();

		classLoader.addTranslator( pool, translator );

		classLoader.run( programName, programArgs );
	}

}

