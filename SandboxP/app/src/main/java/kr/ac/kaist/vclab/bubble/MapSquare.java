package kr.ac.kaist.vclab.bubble;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by avantgarde on 2016-11-02.
 */

public class MapSquare {
    private final int mProgram;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mNormalBuffer;
    private FloatBuffer mTextureCoorBuffer;

    // bitmap (for texture)
    private Bitmap bitmap;

    // attribute handles
    private int mPositionHandle;
    private int mNormalHandle;

    // uniform handles
    private int mProjMatrixHandle;
    private int mModelViewMatrixHandle;
    private int mNormalMatrixHandle;

    private int mLightHandle;
    private int mLight2Handle;
    private int mColorHandle;

    private int mTextureHandle;

    private static final int COORDS_PER_VERTEX = 3;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

    private static MapGenerator mGenerator = new MapGenerator(
            25.0f, 3.0f, 25.0f, // size
            0.3f, // unit length
            12.0f, // max height
            -2.0f, // min height
            3.3f, // complexity
            1.0f, // normalRate
            true // fill or not
    );

    private static float[] vertices = mGenerator.getVertices();
    private static float[] normals = mGenerator.getNormals();
    private static float[] textureCoors = mGenerator.getTextureCoors();
    private static int mode = mGenerator.getMode();

    public float sizeX = mGenerator.sizeX;
    public float sizeY = mGenerator.sizeY;
    public float sizeZ = mGenerator.sizeZ;

    float color[] = {0.33f, 0.42f, 0.18f};

    public MapSquare(Bitmap bitmap) {
        assert vertices.length == textureCoors.length;

        ByteBuffer byteBuf1 = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf1.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf1.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer byteBuf2 = ByteBuffer.allocateDirect(normals.length * 4);
        byteBuf2.order(ByteOrder.nativeOrder());
        mNormalBuffer = byteBuf2.asFloatBuffer();
        mNormalBuffer.put(normals);
        mNormalBuffer.position(0);

        ByteBuffer byteBuf3 = ByteBuffer.allocateDirect(textureCoors.length * 4);
        byteBuf3.order(ByteOrder.nativeOrder());
        mTextureCoorBuffer = byteBuf3.asFloatBuffer();
        mTextureCoorBuffer.put(textureCoors);
        mTextureCoorBuffer.position(0);


        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShaderFromFile(
                GLES20.GL_VERTEX_SHADER, "map.vshader");
        int fragmentShader = MyGLRenderer.loadShaderFromFile(
                GLES20.GL_FRAGMENT_SHADER, "map.fshader");

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        // texture
        int[] textureHandles = new int[1];
        GLES20.glGenTextures(1, textureHandles, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, MyGLRenderer.loadImage("terrain.jpg"), 0);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    /* Constructor without texture. */
    public MapSquare() {
        this(null);
    }

    public void draw(float[] projMatrix,
                     float[] modelViewMatrix,
                     float[] normalMatrix,
                     float[] light,
                     float[] light2) {
        GLES20.glUseProgram(mProgram);

        // uniforms
        mProjMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uProjMatrix");
        mModelViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelViewMatrix");
        mNormalMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMatrix");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        mLightHandle = GLES20.glGetUniformLocation(mProgram, "uLight");
        mLight2Handle = GLES20.glGetUniformLocation(mProgram, "uLight2");
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTextureUnit");

        GLES20.glUniformMatrix4fv(mProjMatrixHandle, 1, false, projMatrix, 0);
        GLES20.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(mNormalMatrixHandle, 1, false, normalMatrix, 0);

        GLES20.glUniform3fv(mColorHandle, 1, color, 0);
        GLES20.glUniform3fv(mLightHandle, 1, light, 0);
        GLES20.glUniform3fv(mLight2Handle, 1, light2, 0);

        // attributes
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoor");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, mVertexBuffer);

        GLES20.glVertexAttribPointer(
                mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, mNormalBuffer);

        GLES20.glVertexAttribPointer(
                mTextureHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                VERTEX_STRIDE, mTextureCoorBuffer
        );

        // set texture
        GLES20.glUniform1i(mTextureHandle, 0);

        // Draw the cube
        GLES20.glDrawArrays(mode, 0, vertices.length / 3);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }
}
