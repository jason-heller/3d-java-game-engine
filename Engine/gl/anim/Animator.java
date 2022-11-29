package gl.anim;

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
import gl.res.Model;
import scene.entity.Entity;
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

	private int startFrame = -1, endFrame = -1;
	
	private Keyframe priorFrame, nextFrame;
	private int lastFrameID = -1;
	
	private Matrix4f[] pose;

	public Animator(Skeleton skeleton, Entity entity) {
		this.entity = entity;
		
		if (skeleton != null) {
			root = skeleton.getRootJoint();
			numJoints = skeleton.getNumJoints();
			AnimationHandler.add(entity);
			entity.setAnimator(this);
			
		} else {
			root = null;
			numJoints = 0;
			
			Console.severe("Animation mismatch, no proper rigging for entity " + entity.getName());
			entity.setModel("error");
			entity.setAnimator(null);
		}
		
	}
	
	public Animator(Model model, Entity entity) {
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
		start(animation, false);
		isPlaying = true;
	}

	public void pause() {
		isPaused = true;
	}
	
	public void unpause() {
		isPaused = false;
	}

	public void start(String animName) {
		start(animName, true, -1, -1);
	}

	public void start(String animName, boolean restartIfPlaying) {
		start(animName, restartIfPlaying, -1, -1);
	}

	public void start(String animName, boolean restartIfPlaying, int startFrame, int endFrame) {
		if (!isPlaying || isPlaying && restartIfPlaying) {
			animationTime = 0;
			animation = Resources.getAnimation(animName);
			
			pose = new Matrix4f[animation.getNumJoints()];
			for(int i = 0; i < pose.length; i++)
				pose[i] = new Matrix4f();
			
			isPlaying = true;

			if (startFrame != -1) {
				this.startFrame = startFrame;
				this.endFrame = endFrame;
				animationTime = animation.getKeyframes()[startFrame].getTime();
			}
			
			lastFrameID = 0;//(int)((animationTime / animation.getDuration()) * animation.getKeyframes().length);
			priorFrame = this.animation.getKeyframes()[0];
			nextFrame = this.animation.getKeyframes()[1];
		}
	}

	public void stop() {
		isPlaying = false;
		isPaused = false;
		isLooping = false;
	}
	
	public void update() {
		
		if (animation != null) {
			if (!isPaused && isPlaying) {
				animationTime = animationTime + Window.deltaTime;

				if (animationTime >= animation.getDuration()) {
					if (!isLooping) {
						stop();
					} else {
						animationTime -= animation.getDuration();
						lastFrameID = 0;
						priorFrame = animation.getKeyframes()[lastFrameID];
						nextFrame = animation.getKeyframes()[lastFrameID + 1];
					}
				}

				if (startFrame != -1) {
					final float endTime = this.animation.getKeyframes()[endFrame].getTime();
					if (animationTime >= endTime) {
						if (!isLooping) {
							stop();
						} else {
							animationTime -= endTime - this.animation.getKeyframes()[startFrame].getTime();
						}
					}
				}
			}
			
			updateAnimation(animationTime);
		}
	}
	
	public static final Quaternion CORRECTION = Quaternion.fromMatrix(new Matrix4f().rotateY(-90f));
	
	private void updateAnimation(float time) {
		
		if (time > nextFrame.getTime()) {
			// Update frames
			lastFrameID++;
			priorFrame = animation.getKeyframes()[lastFrameID];
			nextFrame = animation.getKeyframes()[lastFrameID + 1];
		}
		
		JointTransform rootTransform = getJointTransform((byte)root.index);

		Matrix4f jointMatrix = pose[0];
		jointMatrix.identity();
		jointMatrix.translate(rootTransform.getPosition());
		jointMatrix.rotate(rootTransform.getRotation());
		
		Quaternion q = new Quaternion();
		Quaternion.mul(rootTransform.getRotation(), CORRECTION, q);
		
		root.animPos.set(rootTransform.getPosition());
		root.animRot.set(q);

		applyAnimation(0f, root);
		// drawBones(root);
	}

	// ParentTranform * LocalTransform * InvBindM
	private void applyAnimation(float time, Joint parent) {
		for(Joint child : parent.children) {
			
			JointTransform transform = getJointTransform((byte)child.index);

			Vector3f rotPos = parent.animRot.rotate(transform.getPosition()); // get parents position, after rotation
			Vector3f newPos = Vector3f.add(parent.animPos, rotPos); // add the parents position to this joints position
			
			Quaternion newRot = new Quaternion(); 
			Quaternion.mul(parent.animRot, transform.getRotation(), newRot);
			
			//newRot.normalize();
			
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
	
	/*private void drawBones(Joint parent) {
		for(Joint child : parent.children) {
			
			drawBones(child);
			
			//Vector4f xx = new Vector4f(child.position.x, child.position.y, child.position.z, 1f);
			//pose[child.index].transform(xx);
			//LineRender.drawLine(child.position, new Vector3f(xx));
			LineRender.drawLine(parent.animPos, child.animPos, Colors.WHITE);
			//LineRender.drawLine(parent.position, child.position, Colors.WHITE);
		}
	}*/
	
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
}
