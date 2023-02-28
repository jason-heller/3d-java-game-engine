package dev.cmd;

import static dev.cmd.CommandType.GETTER;
import static dev.cmd.CommandType.SETTER;

import core.App;
import dev.Debug;
import dev.EnvMapBuilder;
import dev.MemDebug;
import gl.Camera;
import gl.Window;
import gl.anim.Animator;
import gl.arc.ArcFaceRender;
import io.Controls;
import map.architecture.ArchitectureHandler;
import scene.entity.util.PhysicsEntity;
import scene.entity.util.PlayerEntity;
import scene.entity.util.SkatePhysicsEntity;
import scene.mapscene.trick.TrickManager;
import ui.UI;
import util.ThirdPersonCameraController;

enum CommandData {
	// Methods
	quit(false),
	exit(false),
	clear(false),
	noclip(true),
	spawn(true, "name", "\"args\""),
	map(false, ArchitectureHandler.validMaps),
	volume(false),
	run(false),
	bind(Controls.class, false, "key", "action"),
	unbind(Controls.class, false, "key"),
	dump_heap(MemDebug.class, true),
	log(false, "message"),
	tp(true, "X, Y, Z"),
	tp_rel(true, "+-X, +-Y, +-Z"),
	fps(false),
	hp(true),
	shadow_quality(false, "0-3"),
	mipmap_bias(false),
	water_quality(false, "0-4"),
	look(false, "yaw", "pitch", "roll"),
	shake(false, "time", "intensity"),
	build_environment_maps(EnvMapBuilder.class, false),
	switch_stance(false),
	rail_mode(true),
	build_raillist(true),
	
	// Getters
	version("VERSION", App.class, GETTER, false),
	os_name("operatingSystem", App.class, GETTER, false),
	os_arch("osArchitecture", App.class, GETTER, false),
	natives_path("nativesPath", App.class, GETTER, false),
	
	// Setters
	enable_console("allowConsole", Debug.class, SETTER, false),
	timescale("timeScale", Window.class, SETTER, false),
	hideui("hideUI", UI.class, SETTER, false),
	debug("debugMode", Debug.class, SETTER, false),
	nav_view("viewNavMesh", Debug.class, SETTER, true),
	nav_path("viewNavPath", Debug.class, SETTER, true),
	nav_node("viewNavNode", Debug.class, SETTER, true),
	nav_points("viewNavPois", Debug.class, SETTER, true),
	light_ambient_only("ambientOnly", Debug.class, SETTER, true),
	view_lightmap("viewLightmapTexture", Debug.class, SETTER, true),
	view_reflect("viewReflectionTexture", Debug.class, SETTER, true),
	view_grindstate_properties("viewGrindState", PlayerEntity.class, SETTER, true),
	shadow_0_tex_view("viewShadowTexture0", Debug.class, SETTER, true),
	face_info("faceInfo", Debug.class, SETTER, true),
	show_hitboxes("showHitboxes", Debug.class, SETTER, true),
	show_collisions("showCollisions", Debug.class, SETTER, true),
	show_bsp_leafs("showLeafs", Debug.class, SETTER, true),
	show_clips("showClips", Debug.class, SETTER, true),
	show_ambient("showAmbient", Debug.class, SETTER, true),
	show_bones("drawBones", Animator.class, SETTER, true),
	show_model_bounds("showModelBounds", Debug.class, SETTER, true),
	room_name("showCurrentRoom", Debug.class, SETTER, true),
	wireframe("wireframeMode", Debug.class, SETTER, true),
	fullbright("fullbright", Debug.class, SETTER, true),
	cam_speed("cameraSpeed", Camera.class, SETTER, true),
	cam_dist("followDistance", ThirdPersonCameraController.class, SETTER, false),
	phys_player_jump("jumpVel", SkatePhysicsEntity.class, SETTER, true),
	phys_player_speed_max("maxSpeed", SkatePhysicsEntity.class, SETTER, true),
	phys_player_speed_air_max("maxAirSpeed", SkatePhysicsEntity.class, SETTER, true),
	phys_player_accel("baseAccelSpeed", SkatePhysicsEntity.class, SETTER, true),
	phys_player_accel_air("airAccel", SkatePhysicsEntity.class, SETTER, true),
	phys_gravity("gravity", PhysicsEntity.class, SETTER, true),
	phys_gravity_max("maxGravity", PhysicsEntity.class, SETTER, true),
	phys_friction("friction", PhysicsEntity.class, SETTER, true),
	phys_friction_air("airFriction", PhysicsEntity.class, SETTER, true),
	god("god", Debug.class, SETTER, true),
	fullrender("fullRender", ArcFaceRender.class, SETTER, false),
	velocity_vectors("velocityVectors", Debug.class, SETTER, true),
	console_blocking("isBlocking", Console.class, SETTER, true),
	c_perfect_grind_balance("perfectGrind", SkatePhysicsEntity.class, SETTER, true);

	Command command;
	
	private CommandData(boolean cheats) {
		command = new Command(name(), cheats);
	}
	
	private CommandData(boolean cheats, String ... paramNames) {
		command = new Command(name(), cheats, paramNames);
	}
	
	private CommandData(Class<?> varLoc, boolean cheats, String ... paramNames) {
		command = new Command(name(), varLoc, cheats, paramNames);
	}
	
	private CommandData(String varName, Class<?> varLoc, CommandType type, boolean cheats, String ...paramNames) {
		command = new Command(name(), varName, varLoc, type, cheats, paramNames);
	}
	
	public static Command getCommand(String name) {
		for(CommandData command : values() ) {
			Command c = command.command;
			if (c.getName().equals(name)) {
				return c;
			}
		}
		
		return null;
	}
}
