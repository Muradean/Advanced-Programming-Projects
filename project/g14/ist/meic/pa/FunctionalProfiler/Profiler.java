package ist.meic.pa.FunctionalProfiler;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

@NoProfile
public class Profiler {

	private enum KEYS {
		READS, WRITES
	};

	private static Profiler INSTANCE;

	/** Map to record every read and write for every class. */
	private Map<String, Map<KEYS, Integer>> map;

	private int totalReads;

	private int totalWrites;

	private Profiler() {
		this.map = new TreeMap<String, Map<KEYS, Integer>>(
			( s1, s2 ) -> s1.compareTo( s2 ) // Default behaviour was `s1.compareToIgnoreCase( s2 )`.
		);
	}

	public static synchronized Profiler getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new Profiler();
		}

		return INSTANCE;
	}

	protected int getTotalReads() {
		return this.totalReads;
	}

	protected int getTotalWrites() {
		return this.totalWrites;
	}

	/**
	 * Method to check if a given class name already has a record. If not,
	 * initialize it with both reads and writes at 0.
	 * @param className The class name to store.
	 */
	private void initCheck( String className ) {
		if ( !this.map.containsKey( className ) ) {
			Map readsWritesMap = new HashMap<KEYS, Integer>();

			readsWritesMap.put( KEYS.READS, 0 );

			readsWritesMap.put( KEYS.WRITES, 0 );

			this.map.put( className, readsWritesMap );
		}
	}

	/**
	 * Increments the number of reads for the given class name.
	 * @param className The class name to which the number of reads will increase.
	 */
	public void incReads( String className ) {
		this.initCheck( className );

		int currReads = this.map.get( className ).get( KEYS.READS );

		this.map.get( className ).put( KEYS.READS, currReads += 1 );

		this.totalReads += 1;
	}

	/**
	 * Increments the number of writes for the given class name.
	 * @param className The class name to which the number of writes will increase.
	 */
	public void incWrites( String className ) {
		this.initCheck( className );

		int currWrites = this.map.get( className ).get( KEYS.WRITES );

		this.map.get( className ).put( KEYS.WRITES, currWrites += 1 );

		this.totalWrites += 1;
	}

	/**
	 * Presents all the info gathered.
	 */
	public void dump() {
		System.out.println(
			String.format(
				"Total reads: %d Total writes: %d",
				this.getTotalReads(),
				this.getTotalWrites()
			)
		);

		this.map.forEach( ( className, classStats ) -> {
			System.out.println(
				String.format(
					"class %s -> reads: %d writes: %d",
					className,
					classStats.get( KEYS.READS ),
					classStats.get( KEYS.WRITES )
				)
			);
		} );
	}
}

