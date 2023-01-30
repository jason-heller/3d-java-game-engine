package map;

/** Needed to prevent type ambiguity
 * 
 */
import java.util.ArrayList;
import java.util.List;

public class RailList {

	public static final int BLOCK_SIZE = 64;		// size in units of each rail square

	public static int numBlocksX, numBlocksZ;
	public static int numBlocks;
	
	private List<Rail> rails = new ArrayList<>();

	public void addRail(Rail rail) {
		rails.add(rail);
	}
	
	public List<Rail> getRails() {
		return rails;
	}

}
