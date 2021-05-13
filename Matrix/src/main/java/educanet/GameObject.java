package educanet;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GameObject {
	private float speed = 0.00025f;

	private FloatBuffer cfb;

	private final int[] indices = {
			0, 1, 3, // First triangle
			1, 2, 3 // Second triangle
	};

	private final float[] colors = {
			1f, 1f, 1f, 1f,
			1f, 1f, 1f, 1f,
			1f, 1f, 1f, 1f,
			1f, 1f, 1f, 1f,
	};

	private final float[] green = {
			1f, 0f, 0f, 1f,
			1f, 0f, 0f, 1f,
			1f, 0f, 0f, 1f,
			1f, 0f, 0f, 1f,
	};
	private final float[] red = {
			0f, 1f, 0f, 1f,
			0f, 1f, 0f, 1f,
			0f, 1f, 0f, 1f,
			0f, 1f, 0f, 1f,
	};


	private float x;
	private float y;
	private float size;


	private final int squareVaoId;
	private final int uniformMatrixLocation;
	private final int squareColorId;

	Matrix4f matrix;
	FloatBuffer matrixFloatBuffer;

	public GameObject(float x, float y, float size) {
		matrix = new Matrix4f().identity();
		matrixFloatBuffer = BufferUtils.createFloatBuffer(16);
		this.x = x;
		this.y = y;
		this.size = size;

		float[] vertices = {
				x + size, y, 0.0f, //right top
				x + size, y - size, 0.0f, //right bottom
				x, y - size, 0.0f, //left bottom
				x, y, 0.0f,//left top
		};


		matrixFloatBuffer = BufferUtils.createFloatBuffer(16);
		matrix = new Matrix4f().identity();

		cfb = BufferUtils.createFloatBuffer(red.length).put(red).flip();

		squareVaoId = GL33.glGenVertexArrays();
		int squareEboId = GL33.glGenBuffers();
		int squareVboId = GL33.glGenBuffers();
		squareColorId = GL33.glGenBuffers();

		uniformMatrixLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");
		GL33.glBindVertexArray(squareVaoId);
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, squareEboId);
		IntBuffer ib = BufferUtils.createIntBuffer(indices.length)
				.put(indices)
				.flip();
		GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ib, GL33.GL_STATIC_DRAW);
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareColorId);
		cfb = BufferUtils.createFloatBuffer(colors.length).put(colors).flip();
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cfb, GL33.GL_STATIC_DRAW);
		GL33.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0);
		GL33.glEnableVertexAttribArray(1);

		MemoryUtil.memFree(cfb);

		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareVboId);
		FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length)
				.put(vertices)
				.flip();

		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
		GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0);
		GL33.glEnableVertexAttribArray(0);
		GL33.glUseProgram(Shaders.shaderProgramId);
		matrix.get(matrixFloatBuffer);
		GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);

		MemoryUtil.memFree(fb);
		MemoryUtil.memFree(ib);
	}

	public void render() {
		matrix.get(matrixFloatBuffer);
		GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);
		GL33.glUseProgram(Shaders.shaderProgramId);
		GL33.glBindVertexArray(squareVaoId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, indices.length, GL33.GL_UNSIGNED_INT, 0);
	}

	public void update(long window) {
		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
			matrix = matrix.translate(0f, speed, 0f);
			y += speed;
		}

		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
			matrix = matrix.translate(0, -speed, 0f);
			y -= speed;
		}

		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
			matrix = matrix.translate(speed, 0f, 0f);
			x += speed;
		}

		if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
			matrix = matrix.translate(-speed, 0f, 0f);
			x -= speed;
		}

		// TODO: Send to GPU only if position updated
		matrix.get(matrixFloatBuffer);
		GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);
	}

	public void setColor(boolean colliding) {
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareColorId);
		cfb.put(colliding ? red : green).flip();

		GL33.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0);
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cfb, GL33.GL_STATIC_DRAW);
		GL33.glEnableVertexAttribArray(1);
	}

}
