package gl.shadow;

import gl.Render;

public class ShadowMeshRender {

	private ShadowShader shader;

	/**
	 * @param shader
	 *            - the simple shader program being used for the shadow render
	 *            pass.
	 * @param projectionViewMatrix
	 *            - the orthographic projection matrix multiplied by the light's
	 *            "view" matrix.
	 */
	protected ShadowMeshRender(ShadowShader shader) {
		this.shader = shader;
	}

	/*protected void render(Matrix4f projectionViewMatrix, List<Entity> entities, List<Chunk> chunks) {
		for (Entity entity : entities) {
			Model model = entity.getModel();
			model.bind(0, 1);
			entity.getDiffuse().bind(0);
			Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, entity.getMatrix(), null);
			shader.projectionViewMatrix.loadMatrix(mvpMatrix);
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(),
					GL11.GL_UNSIGNED_INT, 0);
					Render.drawCalls++;
		}
		
		shader.projectionViewMatrix.loadMatrix(projectionViewMatrix);
		//Resources.getTexture("default").bind(0);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindVertexArray(0);
	}
*/
}
