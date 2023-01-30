package scene.mapscene.trick;

import core.Resources;
import gl.anim.Animation;

public class Trick {

		private String name;
		private Animation animation;
		
		private TrickType type;
		
		private int points;
		private float safeFrame;
		
		private boolean landBackwards, landSwitch;
		public int id;
		
		public Trick(TrickType type, String name, String animName, int points, float safeFrame, int id) {
			this.type = type;
			this.name = name;
			this.animation = Resources.getAnimation(animName);
			this.points = points;
			this.safeFrame = safeFrame;		// After this frame, it is safe to land while still in animation
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public Animation getAnimation() {
			return animation;
		}

		public TrickType getType() {
			return type;
		}
		
		public int getPoints() {
			return points;
		}
		
		public float getSafeFrame() {
			return safeFrame;
		}

		public Trick landBackwardsFlag() {
			landBackwards = true;
			return this;
		}

		public Trick landSwitchFlag() {
			landSwitch = true;
			return this;
		}
		
		public boolean isLandBackwards() {
			return landBackwards;
		}
		
		public boolean isLandSwitch() {
			return landSwitch;
		}
}
