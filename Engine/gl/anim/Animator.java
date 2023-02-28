package gl.anim;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
import util.Colors;
import util.MathUtil;
import util.Vectors;

public class Animator {

	private final Joint root;
	private final int numJoints;

	private Animation animation;
	private float animationTime = 0;
	private boolean isPaused = false;
	private boolean isPlaying = false;

	private final Entity entity;
	
	private Keyframe priorFrame, nextFrame;
	private int lastFrameID = -1;
	
	private Matrix4f[] pose;
	private JointTransform[] currentJointTransform;		// Same info as above, just cached, not necesarily 1-1
	
	private Ragdoll ragdoll;
	
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
			currentJointTransform = new JointTransform[numJoints];
			
			Matrix4f m = new Matrix4f();
			JointTransform jt = new JointTransform(new Vector3f(), new Quaternionf());
			
			for(int i = 0; i < pose.length; i++) {		// HACK : Prevents a bad .DOOM load crash, find better solution
				pose[i] = m;
				currentJointTransform[i] = jt;
			}
			
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

	public void pause() {
		isPaused = true;
	}
	
	public void unpause() {
		isPaused = false;
	}
	
	public void start(String animName) {
		Animation newAnim = Resources.getAnimation(animName);
		
		if (newAnim == null) {
			Console.severe("Animator attempted to load nonexistent animation: " + animName);
			return;
		}
		
		start(newAnim);
	}
	
	public void start(Animation newAnim) {
		if (isPlaying) {
			stop();
		}
	
		pose = new Matrix4f[newAnim.getNumJoints()];
		for(int i = 0; i < pose.length; i++)
			pose[i] = new Matrix4f();
		
		isPlaying = true;
		isPaused = false;
		
		currentAnim = newAnim.getName();
		animation = newAnim;
		
		resetKeyframes();
		animationTime = 0;
		update();
	}

	// Resets the keyframes for looping/init
	// TODO: This shouldn't be called when an animation repeats, as the frame data could just be the last frame
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
		isPaused = true;
	}
	

	public static final Quaternionf CORRECTION = new Quaternionf().setFromNormalized(new Matrix4f().rotateX(-MathUtil.HALFPI));
	
	public void update() {
		
		if (ragdoll != null) {
			ragdoll.applyToPose(pose, root);
			return;
		}
		
		/*UI.drawRect(300, 199, 200, 2, Colors.YELLOW);
		UI.drawRect((int) (300 + ((animationTime / animation.getDuration())*200)), 190, 3, 20, Colors.RED);
		UI.drawString(animationTime + "\n" + animation.getDuration(), 200,150);*/
		
		if (!isPaused)
			animationTime += Window.deltaTime * speedMultiplier;
		
		// Handle animation end
		if (animationTime >= animation.getDuration()) {
			String nextAnim = animation.getNextAnim();
			
			if (!nextAnim.isEmpty()) {
				start(nextAnim);
			} else if (!animation.isLooping()) {
				stop();
				animationTime -= Window.deltaTime * speedMultiplier;
			} else {
				resetKeyframes();
				float duration = animation.getDuration();
				
				if (animation.hasNoTransition())
					duration -= animation.getKeyframes()[0].getTime();
				
				animationTime -= duration;
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

		Quaternionf rootJointQuat = new Quaternionf();
		CORRECTION.mul(rootTransform.getRotation(), rootJointQuat);

		root.animPos.set(rootTransform.getPosition());
		root.animRot.set(rootJointQuat);
		
		currentJointTransform[0] = rootTransform;

		applyAnimation(root);
		
		if (drawBones) {
			drawBones(root);
		}
	}
	
	// ParentTranform * LocalTransform * InvBindM
	private void applyAnimation(Joint parent) {
		for(Joint child : parent.children) {
			
			JointTransform transform = getJointTransform((byte)child.index);

			Vector3f rotPos = rotate(parent.animRot, transform.getPosition()); // get parents position, after rotation
			Vector3f newPos = Vectors.add(parent.animPos, rotPos); // add the parents position to this joints position

			Quaternionf newRot = new Quaternionf(parent.animRot); 
			newRot.mul(transform.getRotation());
			
			Matrix4f jointMatrix = pose[child.index];
			jointMatrix.identity();
			jointMatrix.translate(newPos);
			jointMatrix.rotate(newRot);
			
			currentJointTransform[child.index] = transform;
			
			child.animPos.set(newPos);
			child.animRot.set(newRot);
			
			applyAnimation(child);
		}
		
		pose[parent.index].mul(parent.getInverseBindMatrix());
	}
	
	private Vector3f rotate(Quaternionf q, Vector3f v) {
		Vector3f quatVector = new Vector3f(q.x, q.y, q.z);
		Vector3f uv = Vectors.cross(quatVector, v);
		Vector3f uuv = Vectors.cross(quatVector, uv);
		return Vectors.add(v, Vectors.add(Vectors.mul(uv, q.w), uuv).mul(2f));
	}
	
	private Keyframe getPoseAsKeyframe() {
		Map<Byte, JointTransform> transforms = new HashMap<>();
		int numJoints = animation.getNumJoints();
		
		for (int i = 0; i < numJoints; i++) {
			byte index = (byte)i;
			transforms.put(index, currentJointTransform[i]);
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
			Vector3f parentPos = new Vector3f(parent.animPos);
			Vector3f childPos = new Vector3f(child.animPos);
			
			mat.transformPosition(parentPos);
			mat.transformPosition(childPos);
			
			Vector3f translation = new Vector3f();
			mat.getTranslation(translation);
			
			parentPos.add(translation);
			childPos.add(translation);
			
			LineRender.drawLine(parentPos, childPos, Colors.WHITE);
		
			parentPos = new Vector3f(parent.animPos);
			childPos = new Vector3f(child.animPos);
			
			pose[parent.index].transformPosition(parentPos);
			pose[child.index].transformPosition(childPos);
			
			LineRender.drawLine(parentPos, childPos, Colors.RED);
			
		}
	}
	
	public JointTransform getJointTransform(byte index) {
		JointTransform A = priorFrame.getTransforms().get(index);
		JointTransform B = nextFrame.getTransforms().get(index);
		
		float interp = (animationTime - priorFrame.getTime()) / (nextFrame.getTime() - priorFrame.getTime());

		return JointTransform.lerp(A, B, interp);
	}
	
	public void setRagdoll(Ragdoll ragdoll) {
		this.ragdoll = ragdoll;
	}

	public Matrix4f[] getPose() {
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

	public JointTransform[] getCurrentJointTransforms() {
		return this.currentJointTransform;
	}
}
