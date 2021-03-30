package map;

import org.joml.Vector3f;

import core.Application;
import gl.Camera;
import gl.skybox.Skybox2D;
import gl.terrain.TerrainRender;
import map.ground.Terrain;
import scene.Scene;

public class Enviroment {
	private final Vector3f lightDirection;
	
	//private final SkyboxRenderer skyboxRenderer;
	private final TerrainRender terrainRender;
	
	private Terrain terrain;
	
	public int x;
	public int z;
	
	private Skybox2D skybox;

	static long seed;

	public Enviroment(Scene scene, Vector3f lightDirection) {

		seed = 0;
		skybox = new Skybox2D();

		terrainRender = new TerrainRender();
		
		this.lightDirection = lightDirection; 
		
		terrain = new Terrain(scene.getCamera(), "maps/heightmap.png", 1);

		reposition(x, z);
	}
	
	public void cleanUp() {
		terrain.cleanUp();
		skybox.cleanUp();
		terrainRender.cleanUp();
		//Resources.removeTextureReference("terrain_tiles");
	}

	public void render(Camera camera) {
		
		skybox.render(camera, lightDirection);
		terrainRender.render(camera, lightDirection, terrain);

		//EntityControl.render(camera, lightDirection);
	}
	
	public void reposition(int x, int z) {
		this.x = x;
		this.z = z;
		
		//terrain.populate(shiftX, shiftZ);
	}
	
	public void update(Scene scene) {
		final Camera camera = scene.getCamera();
		terrain.update(camera);
	}
	
	public Vector3f getLightDirection() {
		return lightDirection;
	}

	public Skybox2D getSkybox() {
		return skybox;
	}

	public boolean isFullyLoaded() {
		return terrain.isFullyLoaded();
	}
}
