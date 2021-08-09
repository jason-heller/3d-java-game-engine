package map.architecture.components;

public class ArcRoom {
	public static final ArcRoom DEFAULT_ROOM = null; 	// If you wanted unnamed rooms to have a 'default' room, it'd go
														// here

	private String name;
	private int type;
	private GhostPoi[] pois;
	
	public ArcRoom(String name, int type, GhostPoi[] pois) {
		this.name = name;
		this.type = type;
		this.pois = pois;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public GhostPoi[] getGhostPois() {
		return pois;
	}
}
