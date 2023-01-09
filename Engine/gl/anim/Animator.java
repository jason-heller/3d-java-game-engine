package gl.anim;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternion;
import org.joml.Vector3f;
import org.joml.Vector4f;

import core.Resources;
import dev.cmd.Console;
import gl.Window;
import gl.anim.component.Joint;
import gl.anim.component.JointTransform;
import gl.anim.component.Keyframe;
import gl.anim.component.Skeleton;
import gl.anim.render.AnimationHandler;
import gl.line.LineRender;
import gl.res.Mesh;
import scene.entity.Entity;
import ui.UI;
import util.Colors;
import util.MathUtil;

public class Animator {

	private final Joint root;
	private final int numJoints;

	private Animation animation;
	private float animationTime = 0;
	private boolean isPaused = false;
	private boolean isPlaying = false;

	private final Entity entity;
	private boolean isLooping = false;
	
	private Keyframe priorFrame, nextFrame;
	private int lastFrameID = -1;
	
	private Matrix4f[] pose;
	private float speedMultiplier = 1f;
	
	private String currentAnim;
	
	public static boolean drawBones = false;

	public Animator(Skeleton skeleton, Entity entity) {
		this.entity = entity;
		
		if (skeleton != null) {
			root = skeleton.getRootJoint();
			numJoints = skeleton.getNumJoints();
			AnimationHandler.add(entity);
			entity.setAnimator(this);
			
			pose = new Matrix4f[numJoints];
			for(int i = 0; i < pose.length; i++)		// HACK : Prevents a bad .DOOM load crash, find better solution
				pose[i] = new Matrix4f();
			
		} else {
			root = null;
			numJoints = 0;
			
			Console.severe("Animation mismatch, no proper rigging for entity " + entity.getName());
			entity.setModel(Resources.ERROR);
			entity.setAnimator(null);
		}
		
	}
	
	public Animator(Mesh model, Entity entity) {
		final Skeleton skeleton = model.getSkeleton();
		this.entity = entity;
		this.root = skeleton.getRootJoint();
		this.numJoints = skeleton.getNumJoints();
		AnimationHandler.add(entity);
	}

	public void destroy() {
		AnimationHandler.remove(entity);
	}

	public void loop(String animation) {
		isLooping = true;
		start(animation);
	}

	public void pause() {
		isPaused = true;
	}
	
	public void unpause() {
		isPaused = false;
	}

	public void start(String animName) {
		if (isPlaying) {
			stop();
		}
		
		Animation newAnim = Resources.getAnimation(animName);
		
		if (newAnim == null) {
			Console.severe("Animator attempted to load nonexistant animation: " + animName);
			return;
		}
		
		pose = new Matrix4f[newAnim.getNumJoints()];
		for(int i = 0; i < pose.length; i++)
			pose[i] = new Matrix4f();
		
		isPlaying = true;
		isPaused = false;
		
		currentAnim = animName;
		animation = newAnim;
		
		resetKeyframes();
		animationTime = 0;
		update();
}

	// Resets the keyframes for looping/init
	private void resetKeyframes() {
		
		if (priorFrame != null && (animation.getKeyframes().length == 1 || animation.getKeyframes()[0].getTime() > 0f)) {
			priorFrame = getPoseAsKeyframe();
			nextFrame = animation.getKeyframes()[0];
			lastFrameID = -1;
			return;
		}
	
		priorFrame = animation.getKeyframes()[0];
		nextFrame = animation.getKeyframes()[1];
		lastFrameID = 0;
	}

	public void stop() {

		isPlaying = false;
		isLooping = false;
		isPaused = true;
	}
	

	public static final Quaternion CORRECTION = Quaternion.fromMatrix(new Matrix4f().rotateY(90f));
	
	public void update() {
		
		/*UI.drawRect(300, 199, 200, 2, Colors.YELLOW);
		UI.drawRect((int) (300 + ((animationTime / animation.getDuration())*200)), 190, 3, 20, Colors.RED);
		UI.drawString(animationTime + "\n" + animation.getDuration(), 200,150);*/
		
		if (animation == null || !isPlaying)
			return;
		
		if (!isPaused)
			animationTime += Window.deltaTime * speedMultiplier;
		
		// Handle animation end
		if (animationTime >= animation.getDuration()) {
			if (!isLooping) {
				stop();
				animationTime -= Window.deltaTime * speedMultiplier;
			} else {
				resetKeyframes();
				animationTime -= animation.getDuration();
			}
		}

		// Handle animation keyframe advancement
		if (animationTime > nextFrame.getTime()) {
			// Update frames
			lastFrameID++;
			priorFrame = animation.getKeyframes()[lastFrameID];
			nextFrame = animation.getKeyframes()[lastFrameID + 1];
		}
		
		// Create pose
		JointTransform rootTransform = getJointTransform((byte)root.index);

		Matrix4f jointMatrix = pose[0];
		jointMatrix.identity();
		jointMatrix.translate(rootTransform.getPosition());
		jointMatrix.rotate(rootTransform.getRotation());
		
		Quaternion q = new Quaternion();
		Quaternion.mul(rootTransform.getRotation(), CORRECTION, q);
		
		root.animPos.set(rootTransform.getPosition());
		//root.animRot.set(rootTransform.getRotation());
		root.animRot.set(q);
		

		applyAnimation(0f, root);
		
		if (drawBones) {
			drawBones(root);
		}
	}

	// ParentTranform * LocalTransform * InvBindM
	private void applyAnimation(float time, Joint parent) {
		for(Joint child : parent.children) {
			
			JointTransform transform = getJointTransform((byte)child.index);

			Vector3f rotPos = parent.animRot.rotate(transform.getPosition()); // get parents position, after rotation
			Vector3f newPos = Vector3f.add(parent.animPos, rotPos); // add the parents position to this joints position
			
			Quaternion newRot = new Quaternion(); 
			Quaternion.mul(parent.animRot, transform.getRotation(), newRot);
			
			Matrix4f jointMatrix = pose[child.index];
			jointMatrix.identity();
			jointMatrix.translate(newPos);
			jointMatrix.rotate(newRot);
			
			child.animPos.set(newPos);
			child.animRot.set(newRot);
			
			applyAnimation(time, child);
		}
		
		pose[parent.index].mul(parent.getInverseBindMatrix());
	}
	
	private Keyframe getPoseAsKeyframe() {
		Map<Byte, JointTransform> transforms = new HashMap<>();
		int numJoints = animation.getNumJoints();
		
		for (int i = 0; i < numJoints; i++) {
			byte index = (byte)i;
			transforms.put(index, this.getJointTransform(index));
		}
		
		return new Keyframe(0f, transforms);
	}
	
	private void drawBones(Joint parent) {
		for(Joint child : parent.children) {
			
			drawBones(child);
			
			/*
			 * Uncomment this code for a detailed representation of the rig (at the origin)
				Vector4f xx = new Vector4f(child.position.x, child.position.y, child.position.z, 1f);
				pose[child.index].transform(xx);
				LineRender.drawLine(child.position, new Vector3f(xx));
				LineRender.drawLine(parent.animPos, child.animPos, Colors.WHITE);
				LineRender.drawLine(parent.position, child.position, Colors.WHITE);
			*/
			
			Matrix4f mat = entity.getMatrix();
			Vector3f parentPos = new Vector3f();
			Vector3f childPos = new Vector3f();
			
			Vector3f.mul(parent.animPos, mat, parentPos);
			Vector3f.mul(child.animPos, mat, childPos);
			
			parentPos.add(mat.getTranslation());
			childPos.add(mat.getTranslation());
			
			LineRender.drawLine(parentPos, childPos, Colors.WHITE);
			
			
			
			parentPos = new Vector3f();
			childPos = new Vector3f();
			
			Vector3f.mul(parent.animPos, pose[parent.index], parentPos);
			Vector3f.mul(child.animPos, pose[child.index], childPos);
			
			//parentPos.add(mat.getTranslation());
			//childPos.add(mat.getTranslation());
			
			LineRender.drawLine(parentPos, childPos, Colors.RED);
			
		}
	}
	
	private JointTransform getJointTransform(byte index) {
		JointTransform A = priorFrame.getTransforms().get(index);
		JointTransform B = nextFrame.getTransforms().get(index);
		
		float interp = (animationTime - priorFrame.getTime()) / (nextFrame.getTime() - priorFrame.getTime());
		
		return JointTransform.lerp(A, B, interp);
	}

	public Matrix4f[] getJointTransforms() {
		return pose;
	}
	
	public float getAnimationTime() {
		return animationTime;
	}

	public Entity getEntity() {
		return entity;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
	
	public String getCurrentAnimation() {
		return this.currentAnim;
	}
	
	public float getSpeedMultiplier() {
		return speedMultiplier;
	}

	public void setSpeedMultiplier(float speedMultiplier) {
		this.speedMultiplier = speedMultiplier;
	}
}
