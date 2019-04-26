package ist.meic.pa.FunctionalProfiler;

import java.lang.annotation.*;

@Retention( RetentionPolicy.RUNTIME )
@Target( {
	ElementType.TYPE, ElementType.METHOD
} )  
public @interface NoProfile {}


