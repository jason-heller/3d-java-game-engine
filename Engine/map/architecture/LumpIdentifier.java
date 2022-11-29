package map.architecture;

import java.util.logging.Logger;

public enum LumpIdentifier {
	BSP(1),
	CLIPS(2),
	ENTITIES(3),
	GEOMETRY(4),
	HEIGHTMAP(5),
	LIGHTMAP(6),
	NAVIGATION(7),
	OBJECTS(8),
	OVERLAYS(9),
	TEXTURES(10),
	VISIBILITY(11);
	
	private int id;
	
	LumpIdentifier(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	// Inefficient, unimportant
	public static void assertValuesUnique(Logger logger) {
		LumpIdentifier[] values = LumpIdentifier.values();
		int len = values.length;
		
		for(int i = 0; i < len; i++) {
			LumpIdentifier lump = values[i];
			
			for(int j = 0; j < len; j++) {
				if (i == j)
					continue;
			
				LumpIdentifier otherLump = values[j];
				
				if (lump.id == otherLump.id) {
					logger.severe("Lump " + lump.name() + " has the same ID as " + otherLump.name() + " (" + lump.id + ")");
					System.exit(-1);
				}
			}
		}
	}
}
