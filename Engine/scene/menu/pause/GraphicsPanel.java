package scene.menu.pause;

import org.lwjgl.opengl.DisplayMode;

import core.App;
import gl.Camera;
import gl.Render;
import gl.Window;
import gl.particle.ParticleHandler;
import map.architecture.ActiveLeaves;
import scene.PlayableScene;
import ui.menu.GuiDropdown;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.GuiSpinner;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;
import ui.menu.listener.SliderListener;

public class GraphicsPanel extends GuiPanel {
	private final GuiDropdown resolution;
	private final GuiSlider fov, fps, particleCount, mapGfxQualityCutoff, mipmapBias, fboScale;
	private final GuiSpinner fullscreen, bordered, shadowQuality;

	private final DisplayMode[] resolutions;
	private final String[] resMenuOptions;

	public GraphicsPanel(GuiPanel parent, int x, int y, int width, int height) {
		super(parent, x, y, width, height);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582 / 2, 392);

		add(new GuiLabel(x, y, "#SGeneral"));
		
		resolutions = Window.getDisplayModes();
		int i = 0;
		resMenuOptions = new String[resolutions.length];
		for (final DisplayMode mode : resolutions) {
			resMenuOptions[i++] = mode.getWidth() + "x" + mode.getHeight();
		}

		fov = new GuiSlider(x, y, "fov", 60, 105, Camera.fov, 1);
		fov.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Camera.fov = (int) value;
				App.scene.getCamera().updateProjection();
			}

		});

		fps = new GuiSlider(x, y, "Framerate", 30, 120, Window.maxFramerate, 1);
		fps.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Window.maxFramerate = (int) value;
			}

		});
		add(fps);
		add(fov);

		fullscreen = new GuiSpinner(x, y, "Windowing", Window.fullscreen ? 1 : 0, "Windowed", "Fullscreen");
		fullscreen.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					Window.fullscreen = true;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), true);
				} else {
					Window.fullscreen = false;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), false);
				}
			}
		});
		add(fullscreen);
		
		bordered = new GuiSpinner(x, y, "Border", Window.hasBorder ? 1 : 0, "Bordered", "No Border");
		bordered.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					Window.hasBorder = true;
					Window.setBorder(true);
				} else {
					Window.hasBorder = false;
					Window.setBorder(false);
				}
			}
		});
		add(bordered);

		resolution = new GuiDropdown(0, 0, "resolution", resMenuOptions);

		resolution.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Window.setDisplayMode(resolutions[index]);

				Render.resizeFbos();
			}

		});

		addWithoutLayout(resolution);
		resolution.setPosition(x + 324, fullscreen.y - 4);
		
		fboScale = new GuiSlider(x, y, "Pixel Sampling Scale", 10, 100, Render.scale * 100, 5);
		fboScale.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Render.scale = value / 100f;
				Render.resizeFbos();
			}

		});
		add(fboScale);
		
		addSeparator();

		particleCount = new GuiSlider(x, y, "max particles", 0, 300, ParticleHandler.maxParticles, 1);
		particleCount.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				ParticleHandler.maxParticles = (int) value;
			}

		});
		add(particleCount);
		
		mipmapBias = new GuiSlider(x, y, "Mipmap Distance", -2f, 5f, -Render.defaultBias, .5f);
		mipmapBias.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Render.defaultBias = -value;
				if (App.scene instanceof PlayableScene) {
					PlayableScene s = (PlayableScene)App.scene;
					s.getArchitecture().changeMipmapBias();
				}
			}

		});
		add(mipmapBias);
		
		addSeparator();
		//add(new GuiLabel(x, y, "#SShadows"));
		mapGfxQualityCutoff = new GuiSlider(x, y, "Shader Distance", 100, 300, ActiveLeaves.cutoffDist, 50);
		mapGfxQualityCutoff.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				int dist = (int) value;
				ActiveLeaves.cutoffDist = dist;
			}

		});
		add(mapGfxQualityCutoff);

		shadowQuality = new GuiSpinner(x + 32, y, "Shadow Quality", Render.shadowQuality, "Low", "Medium", "High", "Very High");
		shadowQuality.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				//ShadowRender.shadowQuality = index;
				switch(index) {
				case 0:
					Render.shadowQuality = 0;
					break;
				case 1:
					Render.shadowQuality = 1;
					break;
				case 2:
					Render.shadowQuality = 2;
					break;
				case 3:
					Render.shadowQuality = 3;
					break;
				}
				
				if (index != 0) {
					//ShadowRender.shadowMapSize = 1024 * ((((int)ShadowBox.shadowDistance)/16) + 1); 
					//ShadowRender.updateParams();
				}
				
				if (App.scene instanceof PlayableScene) {
					PlayableScene s = (PlayableScene)App.scene;
					s.getArchitecture().getLightmap().setFiltering(Render.shadowQuality);
				}
			}
		});
		add(shadowQuality);
	}
}
