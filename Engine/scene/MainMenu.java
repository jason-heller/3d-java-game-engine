package scene;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import core.Resources;
import gl.Camera;
import gl.Window;
import gl.entity.EntityRender;
import gl.res.Mesh;
import gl.res.Model;
import gl.res.Texture;
import gl.skybox.Skybox2D;
import io.MusicHandler;
import map.architecture.ArchitectureHandler;
import scene.menu.MainMenuUI;
import util.MathUtil;
import util.Vectors;

public class MainMenu implements Scene {
	
	private static final float X_TILT = .3f;
	private static final float Z_TILT = MathUtil.HALFPI - .3f;
	
	private MainMenuUI ui;
	private Camera camera = new Camera();
	
	private MusicHandler musicHandler;
	
	private Model model, bg;
	private Matrix4f matrix;
	private Vector4f color;
	private float rotAng;
	
	private Skybox2D skybox;
	private Texture texture;
	
	public MainMenu() {
		ui = new MainMenuUI(this);
		ArchitectureHandler.pollValidMaps();		// Poll anytime the menu is opened
		
		Resources.addModel("board", "models/board.MF");
		model = Resources.getModel("board");
		
		musicHandler = new MusicHandler();
		musicHandler.loadQueue(1);
		musicHandler.playNext();

		color = new Vector4f(0, 0, 0, 0);
		matrix = new Matrix4f();
		
		bg = Resources.getModel("cube");
		

		final String root = "sky/";
		texture = Resources.addCubemap("skybox", root + "skybox_menu.png");
		
		skybox = new Skybox2D();
	}
	
	@Override
	public void tick() {
		
	}

	@Override
	public void cleanUp() {
		for(Mesh m : model.getMeshes()) {
			m.cleanUp();
		}
		
		texture.cleanUp();
		skybox.cleanUp();
		
		musicHandler.stopMusic();
		musicHandler.cleanUp();
		ui.cleanUp();
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public void render(Vector4f clipPlane) {
		
		matrix.identity();
		matrix.translate(0f, 0f, -3f);
		matrix.rotate(rotAng, Vectors.POSITIVE_Y);
		matrix.rotate(Z_TILT, Vectors.POSITIVE_Z);
		matrix.rotate(X_TILT, Vectors.POSITIVE_X);
		
		EntityRender.render(camera, matrix, color, model);
		skybox.render(null, camera);
	}
	
	@Override
	public void renderNoReflect() {
	}
	
	@Override
	public void update() {
		
		rotAng += Window.deltaTime;
		
		ui.update();
	}

	@Override
	public void postRender() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fastRender(Vector4f vector4f) {
		// TODO Auto-generated method stub
		
	}
}
