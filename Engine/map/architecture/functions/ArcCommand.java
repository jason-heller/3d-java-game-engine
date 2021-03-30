package map.architecture.functions;

import map.architecture.functions.commands.SpawnPoint;

/**
 * Arc Commands are commands that can be called to trigger certain events on the map globally.
 */
public enum ArcCommand {
	SPAWN_PLAYER(SpawnPoint.class, ArcFuncCallMethod.BY_RANDOM);
	
	private Class<? extends ArcFunction> funcClass;
	private ArcFuncCallMethod preferredCallMethod;
	
	/**
	 * @param funcClass is the associated ArcFunction class that handles this command (must extend ArcFunction)
	 * @param preferredCallMethod is the calling method this function will use when no method is specified (UNSPECIFIED)
	 */
	ArcCommand(Class<? extends ArcFunction> funcClass, ArcFuncCallMethod preferredCallMethod) {
		this.funcClass = funcClass;
		this.preferredCallMethod = preferredCallMethod;
	}

	public Class<? extends ArcFunction> getArcFuncClass() {
		return funcClass;
	}
	
	public ArcFuncCallMethod getPrefCallMethod() {
		return preferredCallMethod;
	}
}
