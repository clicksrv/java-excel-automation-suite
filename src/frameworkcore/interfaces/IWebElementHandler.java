package frameworkcore.interfaces;

import frameworkcore.datablocks.ScriptActionBlock;

/**
 * @author Saurav Kumar Sahoo (saurav.kumar.sahoo)
 */
public interface IWebElementHandler {

	/**
	 * This method is to be provided with a ScriptActionBlock, based on which it
	 * will perform the over-ridden actions of the respective Element Handler with this method.
	 * 
	 * @param actionBlock - Outside of a script being read, new
	 *                    ScriptActionBlock(with custom parameters) can be
	 *                    constructed and sent
	 */
	public void handleElement(ScriptActionBlock actionBlock);

}
