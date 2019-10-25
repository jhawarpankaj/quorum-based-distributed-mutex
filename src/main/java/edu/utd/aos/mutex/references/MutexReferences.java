package edu.utd.aos.mutex.references;

/**
 * Constants and references used across project.
 * 
 * @author pankaj
 *
 */
public class MutexReferences {

	/**
	 * System Property that provides URI for the config.
	 */
	public static final String KEY_MUTEX_CONFIG = "mutex.config";
	
	/**
	 * Exit code for errors and exception.
	 */
	public static final int CONST_CODE_ERROR = 1;
	
	/**
	 * Separator used across message transfer between nodes. Backslash for Regex Expression.
	 */
	public static final String SEPARATOR = "\\|\\|";
	
	/**
	 * Separator.
	 */
	public static final String SEPARATOR_TEXT = "||";
	
	public static final String GRANT = "GRANT";
	public static final String REQUEST = "REQUEST";
	public static final String RELEASE = "RELEASE";
	
	/**
	 * Private constructor for utility class.
	 */
	private MutexReferences() {
		
	}
}
