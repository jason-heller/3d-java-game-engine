package dev;

import static dev.CommandType.GETTER;
import static dev.CommandType.SETTER;

import core.Application;
import gl.Camera;
import gl.Window;
import io.Controls;
import map.architecture.ArchitectureHandler;
import scene.entity.utility.PhysicsEntity;
import scene.entity.utility.PlayerHandler;
import ui.UI;

enum CommandData {
	// Methods
	quit(false),
	exit(false),
	noclip(true),
	spawn_monster(true),
	map(false, ArchitectureHandler.validMaps),
	volume(false),
	run(false),
	bind(Controls.class, false, "key", "action"),
	unbind(Controls.class, false, "key"),
	log(false, "message"),
	tp(true, "X, Y, Z"),
	tp_rel(true, "+-X, +-Y, +-Z"),
	fps(false),
	hp(true),
	kill(false),
	hurt(false, "damage", "part"),
	heal(false, "hp", "part"),
	has_walker(false),
	shadow_quality(false, "0-3"),
	
	// Getters
	version("VERSION", Application.class, GETTER, false),
	
	// Setters
	timescale("timeScale", Window.class, SETTER, false),
	hideui("hideUI", UI.class, SETTER, false),
	debug("debugMode", Debug.class, SETTER, false),
	nav_view("viewNavMesh", Debug.class, SETTER, true),
	nav_path("viewNavPath", Debug.class, SETTER, true),
	show_hitboxes("showHitboxes", Debug.class, SETTER, true),
	light_ambient_only("ambientOnly", Debug.class, SETTER, true),
	light_tex_view("viewLightmapTexture", Debug.class, SETTER, true),
	face_info("faceInfo", Debug.class, SETTER, true),
	show_collisions("viewCollide", Debug.class, SETTER, true),
	show_bsp_leafs("showLeafs", Debug.class, SETTER, true),
	show_clips("showClips", Debug.class, SETTER, true),
	show_ambient("showAmbient", Debug.class, SETTER, true),
	wireframe("wireframeMode", Debug.class, SETTER, true),
	fullbright("fullbright", Debug.class, SETTER, true),
	cam_speed("cameraSpeed", Camera.class, SETTER, true),
	cam_sway("swayFactor", Camera.class, SETTER, false),
	phys_player_jump_velocity("jumpVel", PlayerHandler.class, SETTER, true),
	phys_player_friction("friction", PlayerHandler.class, SETTER, true),
	phys_player_friction_air("airFriction", PlayerHandler.class, SETTER, true),
	phys_player_speed_max("maxSpeed", PlayerHandler.class, SETTER, true),
	phys_player_speed_air_max("maxAirSpeed", PlayerHandler.class, SETTER, true),
	phys_player_speed_water_max("maxWaterSpeed", PlayerHandler.class, SETTER, true),
	phys_player_accel("accelSpeed", PlayerHandler.class, SETTER, true),
	phys_player_accel_air("airAccel", PlayerHandler.class, SETTER, true),
	phys_player_accel_water("waterAccel", PlayerHandler.class, SETTER, true),
	phys_gravity("gravity", PhysicsEntity.class, SETTER, true),
	phys_gravity_max("maxGravity", PhysicsEntity.class, SETTER, true),
	god("god", Debug.class, SETTER, true);

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
