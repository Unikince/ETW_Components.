package com.maxelus.gdx.backends.android.livewallpaper.surfaceview;

import com.mybadlogic.gdx.graphics.GL10;
import com.mybadlogic.gdx.graphics.GL11;
import com.mybadlogic.gdx.graphics.GL20;

import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL;

class GLLogWrapper extends GLWrapperBase {
    private static final int FORMAT_FIXED = 2;
    private static final int FORMAT_FLOAT = 1;
    private static final int FORMAT_INT = 0;
    private int mArgCount;
    boolean mColorArrayEnabled;
    private PointerInfo mColorPointer;
    private Writer mLog;
    private boolean mLogArgumentNames;
    boolean mNormalArrayEnabled;
    private PointerInfo mNormalPointer;
    StringBuilder mStringBuilder;
    private PointerInfo mTexCoordPointer;
    boolean mTextureCoordArrayEnabled;
    boolean mVertexArrayEnabled;
    private PointerInfo mVertexPointer;

    private class PointerInfo {
        public Buffer mPointer;
        public int mSize;
        public int mStride;
        public ByteBuffer mTempByteBuffer;
        public int mType;

        public PointerInfo(int size, int type, int stride, Buffer pointer) {
            this.mSize = size;
            this.mType = type;
            this.mStride = stride;
            this.mPointer = pointer;
        }

        public int sizeof(int type) {
            switch (type) {
                case 5120:
                case 5121:
                    return 1;
                case 5122:
                    return 2;
                case 5126:
                    return 4;
                case GL20.GL_FIXED /*5132*/:
                    return 4;
                default:
                    return 0;
            }
        }

        public int getStride() {
            return this.mStride > 0 ? this.mStride : sizeof(this.mType) * this.mSize;
        }

        public void bindByteBuffer() {
            this.mTempByteBuffer = GLLogWrapper.this.toByteBuffer(-1, this.mPointer);
        }

        public void unbindByteBuffer() {
            this.mTempByteBuffer = null;
        }
    }

    public GLLogWrapper(GL gl, Writer log, boolean logArgumentNames) {
        super(gl);
        this.mLog = log;
        this.mLogArgumentNames = logArgumentNames;
    }

    private void checkError() {
        int glError = this.mgl.glGetError();
        if (glError != 0) {
            logLine("glError: " + Integer.toString(glError));
        }
    }

    private void logLine(String message) {
        log(new StringBuilder(String.valueOf(message)).append('\n').toString());
    }

    private void log(String message) {
        try {
            this.mLog.write(message);
        } catch (IOException e) {
        }
    }

    private void begin(String name) {
        log(new StringBuilder(String.valueOf(name)).append('(').toString());
        this.mArgCount = 0;
    }

    private void arg(String name, String value) {
        int i = this.mArgCount;
        this.mArgCount = i + 1;
        if (i > 0) {
            log(", ");
        }
        if (this.mLogArgumentNames) {
            log(new StringBuilder(String.valueOf(name)).append("=").toString());
        }
        log(value);
    }

    private void end() {
        log(");\n");
        flush();
    }

    private void flush() {
        try {
            this.mLog.flush();
        } catch (IOException e) {
            this.mLog = null;
        }
    }

    private void arg(String name, boolean value) {
        arg(name, Boolean.toString(value));
    }

    private void arg(String name, int value) {
        arg(name, Integer.toString(value));
    }

    private void arg(String name, float value) {
        arg(name, Float.toString(value));
    }

    private void returns(String result) {
        log(") returns " + result + ";\n");
        flush();
    }

    private void returns(int result) {
        returns(Integer.toString(result));
    }

    private void arg(String name, int n, int[] arr, int offset) {
        arg(name, toString(n, 0, arr, offset));
    }

    private void arg(String name, int n, short[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void arg(String name, int n, float[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void formattedAppend(StringBuilder buf, int value, int format) {
        switch (format) {
            case 0:
                buf.append(value);
                return;
            case 1:
                buf.append(Float.intBitsToFloat(value));
                return;
            case 2:
                buf.append(((float) value) / 65536.0f);
                return;
            default:
                return;
        }
    }

    private String toString(int n, int format, int[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                formattedAppend(buf, arr[index], format);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, short[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, float[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append("[" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, FloatBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + buf.get(i) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, int format, IntBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = ");
            formattedAppend(builder, buf.get(i), format);
            builder.append('\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, ShortBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + buf.get(i) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private void arg(String name, int n, FloatBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void arg(String name, int n, IntBuffer buf) {
        arg(name, toString(n, 0, buf));
    }

    private void arg(String name, int n, ShortBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void argPointer(int size, int type, int stride, Buffer pointer) {
        arg("size", size);
        arg("type", getPointerTypeName(type));
        arg("stride", stride);
        arg("pointer", pointer.toString());
    }

    private static String getHex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    public static String getErrorString(int error) {
        switch (error) {
            case 0:
                return "GL_NO_ERROR";
            case 1280:
                return "GL_INVALID_ENUM";
            case 1281:
                return "GL_INVALID_VALUE";
            case 1282:
                return "GL_INVALID_OPERATION";
            case GL10.GL_STACK_OVERFLOW /*1283*/:
                return "GL_STACK_OVERFLOW";
            case GL10.GL_STACK_UNDERFLOW /*1284*/:
                return "GL_STACK_UNDERFLOW";
            case 1285:
                return "GL_OUT_OF_MEMORY";
            default:
                return getHex(error);
        }
    }

    private String getClearBufferMask(int mask) {
        StringBuilder b = new StringBuilder();
        if ((mask & 256) != 0) {
            b.append("GL_DEPTH_BUFFER_BIT");
            mask &= -257;
        }
        if ((mask & 1024) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_STENCIL_BUFFER_BIT");
            mask &= -1025;
        }
        if ((mask & 16384) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_COLOR_BUFFER_BIT");
            mask &= -16385;
        }
        if (mask != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append(getHex(mask));
        }
        return b.toString();
    }

    private String getFactor(int factor) {
        switch (factor) {
            case 0:
                return "GL_ZERO";
            case 1:
                return "GL_ONE";
            case 768:
                return "GL_SRC_COLOR";
            case 769:
                return "GL_ONE_MINUS_SRC_COLOR";
            case 770:
                return "GL_SRC_ALPHA";
            case 771:
                return "GL_ONE_MINUS_SRC_ALPHA";
            case 772:
                return "GL_DST_ALPHA";
            case 773:
                return "GL_ONE_MINUS_DST_ALPHA";
            case 774:
                return "GL_DST_COLOR";
            case 775:
                return "GL_ONE_MINUS_DST_COLOR";
            case 776:
                return "GL_SRC_ALPHA_SATURATE";
            default:
                return getHex(factor);
        }
    }

    private String getShadeModel(int model) {
        switch (model) {
            case GL10.GL_FLAT /*7424*/:
                return "GL_FLAT";
            case GL10.GL_SMOOTH /*7425*/:
                return "GL_SMOOTH";
            default:
                return getHex(model);
        }
    }

    private String getTextureTarget(int target) {
        switch (target) {
            case 3553:
                return "GL_TEXTURE_2D";
            default:
                return getHex(target);
        }
    }

    private String getTextureEnvTarget(int target) {
        switch (target) {
            case GL10.GL_TEXTURE_ENV /*8960*/:
                return "GL_TEXTURE_ENV";
            default:
                return getHex(target);
        }
    }

    private String getTextureEnvPName(int pname) {
        switch (pname) {
            case GL10.GL_TEXTURE_ENV_MODE /*8704*/:
                return "GL_TEXTURE_ENV_MODE";
            case GL10.GL_TEXTURE_ENV_COLOR /*8705*/:
                return "GL_TEXTURE_ENV_COLOR";
            default:
                return getHex(pname);
        }
    }

    private int getTextureEnvParamCount(int pname) {
        switch (pname) {
            case GL10.GL_TEXTURE_ENV_MODE /*8704*/:
                return 1;
            case GL10.GL_TEXTURE_ENV_COLOR /*8705*/:
                return 4;
            default:
                return 0;
        }
    }

    private String getTextureEnvParamName(float param) {
        int iparam = (int) param;
        if (param != ((float) iparam)) {
            return Float.toString(param);
        }
        switch (iparam) {
            case GL10.GL_ADD /*260*/:
                return "GL_ADD";
            case 3042:
                return "GL_BLEND";
            case 7681:
                return "GL_REPLACE";
            case GL10.GL_MODULATE /*8448*/:
                return "GL_MODULATE";
            case GL10.GL_DECAL /*8449*/:
                return "GL_DECAL";
            case GL11.GL_COMBINE /*34160*/:
                return "GL_COMBINE";
            default:
                return getHex(iparam);
        }
    }

    private String getMatrixMode(int matrixMode) {
        switch (matrixMode) {
            case GL10.GL_MODELVIEW /*5888*/:
                return "GL_MODELVIEW";
            case GL10.GL_PROJECTION /*5889*/:
                return "GL_PROJECTION";
            case 5890:
                return "GL_TEXTURE";
            default:
                return getHex(matrixMode);
        }
    }

    private String getClientState(int clientState) {
        switch (clientState) {
            case GL10.GL_VERTEX_ARRAY /*32884*/:
                return "GL_VERTEX_ARRAY";
            case GL10.GL_NORMAL_ARRAY /*32885*/:
                return "GL_NORMAL_ARRAY";
            case GL10.GL_COLOR_ARRAY /*32886*/:
                return "GL_COLOR_ARRAY";
            case GL10.GL_TEXTURE_COORD_ARRAY /*32888*/:
                return "GL_TEXTURE_COORD_ARRAY";
            default:
                return getHex(clientState);
        }
    }

    private String getCap(int cap) {
        switch (cap) {
            case GL10.GL_POINT_SMOOTH /*2832*/:
                return "GL_POINT_SMOOTH";
            case GL10.GL_LINE_SMOOTH /*2848*/:
                return "GL_LINE_SMOOTH";
            case 2884:
                return "GL_CULL_FACE";
            case GL10.GL_LIGHTING /*2896*/:
                return "GL_LIGHTING";
            case GL10.GL_COLOR_MATERIAL /*2903*/:
                return "GL_COLOR_MATERIAL";
            case GL10.GL_FOG /*2912*/:
                return "GL_FOG";
            case 2929:
                return "GL_DEPTH_TEST";
            case 2960:
                return "GL_STENCIL_TEST";
            case GL10.GL_NORMALIZE /*2977*/:
                return "GL_NORMALIZE";
            case GL10.GL_ALPHA_TEST /*3008*/:
                return "GL_ALPHA_TEST";
            case 3024:
                return "GL_DITHER";
            case 3042:
                return "GL_BLEND";
            case GL10.GL_COLOR_LOGIC_OP /*3058*/:
                return "GL_COLOR_LOGIC_OP";
            case 3089:
                return "GL_SCISSOR_TEST";
            case 3553:
                return "GL_TEXTURE_2D";
            case 16384:
                return "GL_LIGHT0";
            case GL10.GL_LIGHT1 /*16385*/:
                return "GL_LIGHT1";
            case 16386:
                return "GL_LIGHT2";
            case GL10.GL_LIGHT3 /*16387*/:
                return "GL_LIGHT3";
            case GL10.GL_LIGHT4 /*16388*/:
                return "GL_LIGHT4";
            case GL10.GL_LIGHT5 /*16389*/:
                return "GL_LIGHT5";
            case GL10.GL_LIGHT6 /*16390*/:
                return "GL_LIGHT6";
            case GL10.GL_LIGHT7 /*16391*/:
                return "GL_LIGHT7";
            case GL10.GL_RESCALE_NORMAL /*32826*/:
                return "GL_RESCALE_NORMAL";
            case GL10.GL_VERTEX_ARRAY /*32884*/:
                return "GL_VERTEX_ARRAY";
            case GL10.GL_NORMAL_ARRAY /*32885*/:
                return "GL_NORMAL_ARRAY";
            case GL10.GL_COLOR_ARRAY /*32886*/:
                return "GL_COLOR_ARRAY";
            case GL10.GL_TEXTURE_COORD_ARRAY /*32888*/:
                return "GL_TEXTURE_COORD_ARRAY";
            case GL10.GL_MULTISAMPLE /*32925*/:
                return "GL_MULTISAMPLE";
            case 32926:
                return "GL_SAMPLE_ALPHA_TO_COVERAGE";
            case GL10.GL_SAMPLE_ALPHA_TO_ONE /*32927*/:
                return "GL_SAMPLE_ALPHA_TO_ONE";
            case 32928:
                return "GL_SAMPLE_COVERAGE";
            default:
                return getHex(cap);
        }
    }

    private String getTexturePName(int pname) {
        switch (pname) {
            case 10240:
                return "GL_TEXTURE_MAG_FILTER";
            case 10241:
                return "GL_TEXTURE_MIN_FILTER";
            case 10242:
                return "GL_TEXTURE_WRAP_S";
            case 10243:
                return "GL_TEXTURE_WRAP_T";
            case 33169:
                return "GL_GENERATE_MIPMAP";
            case 35741:
                return "GL_TEXTURE_CROP_RECT_OES";
            default:
                return getHex(pname);
        }
    }

    private String getTextureParamName(float param) {
        int iparam = (int) param;
        if (param != ((float) iparam)) {
            return Float.toString(param);
        }
        switch (iparam) {
            case 9728:
                return "GL_NEAREST";
            case 9729:
                return "GL_LINEAR";
            case 9984:
                return "GL_NEAREST_MIPMAP_NEAREST";
            case 9985:
                return "GL_LINEAR_MIPMAP_NEAREST";
            case 9986:
                return "GL_NEAREST_MIPMAP_LINEAR";
            case 9987:
                return "GL_LINEAR_MIPMAP_LINEAR";
            case 10497:
                return "GL_REPEAT";
            case 33071:
                return "GL_CLAMP_TO_EDGE";
            default:
                return getHex(iparam);
        }
    }

    private String getFogPName(int pname) {
        switch (pname) {
            case GL10.GL_FOG_DENSITY /*2914*/:
                return "GL_FOG_DENSITY";
            case GL10.GL_FOG_START /*2915*/:
                return "GL_FOG_START";
            case GL10.GL_FOG_END /*2916*/:
                return "GL_FOG_END";
            case GL10.GL_FOG_MODE /*2917*/:
                return "GL_FOG_MODE";
            case GL10.GL_FOG_COLOR /*2918*/:
                return "GL_FOG_COLOR";
            default:
                return getHex(pname);
        }
    }

    private int getFogParamCount(int pname) {
        switch (pname) {
            case GL10.GL_FOG_DENSITY /*2914*/:
            case GL10.GL_FOG_START /*2915*/:
            case GL10.GL_FOG_END /*2916*/:
            case GL10.GL_FOG_MODE /*2917*/:
                return 1;
            case GL10.GL_FOG_COLOR /*2918*/:
                return 4;
            default:
                return 0;
        }
    }

    private String getBeginMode(int mode) {
        switch (mode) {
            case 0:
                return "GL_POINTS";
            case 1:
                return "GL_LINES";
            case 2:
                return "GL_LINE_LOOP";
            case 3:
                return "GL_LINE_STRIP";
            case 4:
                return "GL_TRIANGLES";
            case 5:
                return "GL_TRIANGLE_STRIP";
            case 6:
                return "GL_TRIANGLE_FAN";
            default:
                return getHex(mode);
        }
    }

    private String getIndexType(int type) {
        switch (type) {
            case 5121:
                return "GL_UNSIGNED_BYTE";
            case 5123:
                return "GL_UNSIGNED_SHORT";
            default:
                return getHex(type);
        }
    }

    private String getIntegerStateName(int pname) {
        switch (pname) {
            case GL10.GL_SMOOTH_POINT_SIZE_RANGE /*2834*/:
                return "GL_SMOOTH_POINT_SIZE_RANGE";
            case GL10.GL_SMOOTH_LINE_WIDTH_RANGE /*2850*/:
                return "GL_SMOOTH_LINE_WIDTH_RANGE";
            case GL10.GL_MAX_LIGHTS /*3377*/:
                return "GL_MAX_LIGHTS";
            case 3379:
                return "GL_MAX_TEXTURE_SIZE";
            case GL10.GL_MAX_MODELVIEW_STACK_DEPTH /*3382*/:
                return "GL_MAX_MODELVIEW_STACK_DEPTH";
            case GL10.GL_MAX_PROJECTION_STACK_DEPTH /*3384*/:
                return "GL_MAX_PROJECTION_STACK_DEPTH";
            case GL10.GL_MAX_TEXTURE_STACK_DEPTH /*3385*/:
                return "GL_MAX_TEXTURE_STACK_DEPTH";
            case 3386:
                return "GL_MAX_VIEWPORT_DIMS";
            case 3408:
                return "GL_SUBPIXEL_BITS";
            case 3410:
                return "GL_RED_BITS";
            case 3411:
                return "GL_GREEN_BITS";
            case 3412:
                return "GL_BLUE_BITS";
            case 3413:
                return "GL_ALPHA_BITS";
            case 3414:
                return "GL_DEPTH_BITS";
            case 3415:
                return "GL_STENCIL_BITS";
            case 33000:
                return "GL_MAX_ELEMENTS_VERTICES";
            case 33001:
                return "GL_MAX_ELEMENTS_INDICES";
            case 33901:
                return "GL_ALIASED_POINT_SIZE_RANGE";
            case 33902:
                return "GL_ALIASED_LINE_WIDTH_RANGE";
            case GL10.GL_MAX_TEXTURE_UNITS /*34018*/:
                return "GL_MAX_TEXTURE_UNITS";
            case 34466:
                return "GL_NUM_COMPRESSED_TEXTURE_FORMATS";
            case 34467:
                return "GL_COMPRESSED_TEXTURE_FORMATS";
            case 35213:
                return "GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES";
            case 35214:
                return "GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES";
            case 35215:
                return "GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES";
            default:
                return getHex(pname);
        }
    }

    private int getIntegerStateSize(int pname) {
        switch (pname) {
            case GL10.GL_SMOOTH_POINT_SIZE_RANGE /*2834*/:
                return 2;
            case GL10.GL_SMOOTH_LINE_WIDTH_RANGE /*2850*/:
                return 2;
            case GL10.GL_MAX_LIGHTS /*3377*/:
            case 3379:
            case GL10.GL_MAX_MODELVIEW_STACK_DEPTH /*3382*/:
            case GL10.GL_MAX_PROJECTION_STACK_DEPTH /*3384*/:
            case GL10.GL_MAX_TEXTURE_STACK_DEPTH /*3385*/:
            case 3408:
            case 3410:
            case 3411:
            case 3412:
            case 3413:
            case 3414:
            case 3415:
            case 33000:
            case 33001:
            case GL10.GL_MAX_TEXTURE_UNITS /*34018*/:
            case 34466:
                return 1;
            case 3386:
                return 2;
            case 33901:
                return 2;
            case 33902:
                return 2;
            case 34467:
                int[] buffer = new int[1];
                this.mgl.glGetIntegerv(34466, buffer, 0);
                return buffer[0];
            case 35213:
            case 35214:
            case 35215:
                return 16;
            default:
                return 0;
        }
    }

    private int getIntegerStateFormat(int pname) {
        switch (pname) {
            case 35213:
            case 35214:
            case 35215:
                return 1;
            default:
                return 0;
        }
    }

    private String getHintTarget(int target) {
        switch (target) {
            case GL10.GL_PERSPECTIVE_CORRECTION_HINT /*3152*/:
                return "GL_PERSPECTIVE_CORRECTION_HINT";
            case GL10.GL_POINT_SMOOTH_HINT /*3153*/:
                return "GL_POINT_SMOOTH_HINT";
            case GL10.GL_LINE_SMOOTH_HINT /*3154*/:
                return "GL_LINE_SMOOTH_HINT";
            case 3155:
                return "GL_POLYGON_SMOOTH_HINT";
            case GL10.GL_FOG_HINT /*3156*/:
                return "GL_FOG_HINT";
            case 33170:
                return "GL_GENERATE_MIPMAP_HINT";
            default:
                return getHex(target);
        }
    }

    private String getHintMode(int mode) {
        switch (mode) {
            case 4352:
                return "GL_DONT_CARE";
            case 4353:
                return "GL_FASTEST";
            case 4354:
                return "GL_NICEST";
            default:
                return getHex(mode);
        }
    }

    private String getFaceName(int face) {
        switch (face) {
            case 1032:
                return "GL_FRONT_AND_BACK";
            default:
                return getHex(face);
        }
    }

    private String getMaterialPName(int pname) {
        switch (pname) {
            case GL10.GL_AMBIENT /*4608*/:
                return "GL_AMBIENT";
            case GL10.GL_DIFFUSE /*4609*/:
                return "GL_DIFFUSE";
            case GL10.GL_SPECULAR /*4610*/:
                return "GL_SPECULAR";
            case GL10.GL_EMISSION /*5632*/:
                return "GL_EMISSION";
            case GL10.GL_SHININESS /*5633*/:
                return "GL_SHININESS";
            case GL10.GL_AMBIENT_AND_DIFFUSE /*5634*/:
                return "GL_AMBIENT_AND_DIFFUSE";
            default:
                return getHex(pname);
        }
    }

    private int getMaterialParamCount(int pname) {
        switch (pname) {
            case GL10.GL_AMBIENT /*4608*/:
            case GL10.GL_DIFFUSE /*4609*/:
            case GL10.GL_SPECULAR /*4610*/:
            case GL10.GL_EMISSION /*5632*/:
            case GL10.GL_AMBIENT_AND_DIFFUSE /*5634*/:
                return 4;
            case GL10.GL_SHININESS /*5633*/:
                return 1;
            default:
                return 0;
        }
    }

    private String getLightName(int light) {
        if (light < 16384 || light > GL10.GL_LIGHT7) {
            return getHex(light);
        }
        return "GL_LIGHT" + Integer.toString(light);
    }

    private String getLightPName(int pname) {
        switch (pname) {
            case GL10.GL_AMBIENT /*4608*/:
                return "GL_AMBIENT";
            case GL10.GL_DIFFUSE /*4609*/:
                return "GL_DIFFUSE";
            case GL10.GL_SPECULAR /*4610*/:
                return "GL_SPECULAR";
            case GL10.GL_POSITION /*4611*/:
                return "GL_POSITION";
            case GL10.GL_SPOT_DIRECTION /*4612*/:
                return "GL_SPOT_DIRECTION";
            case GL10.GL_SPOT_EXPONENT /*4613*/:
                return "GL_SPOT_EXPONENT";
            case GL10.GL_SPOT_CUTOFF /*4614*/:
                return "GL_SPOT_CUTOFF";
            case GL10.GL_CONSTANT_ATTENUATION /*4615*/:
                return "GL_CONSTANT_ATTENUATION";
            case GL10.GL_LINEAR_ATTENUATION /*4616*/:
                return "GL_LINEAR_ATTENUATION";
            case GL10.GL_QUADRATIC_ATTENUATION /*4617*/:
                return "GL_QUADRATIC_ATTENUATION";
            default:
                return getHex(pname);
        }
    }

    private int getLightParamCount(int pname) {
        switch (pname) {
            case GL10.GL_AMBIENT /*4608*/:
            case GL10.GL_DIFFUSE /*4609*/:
            case GL10.GL_SPECULAR /*4610*/:
            case GL10.GL_POSITION /*4611*/:
                return 4;
            case GL10.GL_SPOT_DIRECTION /*4612*/:
                return 3;
            case GL10.GL_SPOT_EXPONENT /*4613*/:
                return 1;
            case GL10.GL_SPOT_CUTOFF /*4614*/:
                return 1;
            case GL10.GL_CONSTANT_ATTENUATION /*4615*/:
                return 1;
            case GL10.GL_LINEAR_ATTENUATION /*4616*/:
                return 1;
            case GL10.GL_QUADRATIC_ATTENUATION /*4617*/:
                return 1;
            default:
                return 0;
        }
    }

    private String getLightModelPName(int pname) {
        switch (pname) {
            case GL10.GL_LIGHT_MODEL_TWO_SIDE /*2898*/:
                return "GL_LIGHT_MODEL_TWO_SIDE";
            case GL10.GL_LIGHT_MODEL_AMBIENT /*2899*/:
                return "GL_LIGHT_MODEL_AMBIENT";
            default:
                return getHex(pname);
        }
    }

    private int getLightModelParamCount(int pname) {
        switch (pname) {
            case GL10.GL_LIGHT_MODEL_TWO_SIDE /*2898*/:
                return 1;
            case GL10.GL_LIGHT_MODEL_AMBIENT /*2899*/:
                return 4;
            default:
                return 0;
        }
    }

    private String getPointerTypeName(int type) {
        switch (type) {
            case 5120:
                return "GL_BYTE";
            case 5121:
                return "GL_UNSIGNED_BYTE";
            case 5122:
                return "GL_SHORT";
            case 5126:
                return "GL_FLOAT";
            case GL20.GL_FIXED /*5132*/:
                return "GL_FIXED";
            default:
                return getHex(type);
        }
    }

    ByteBuffer toByteBuffer(int byteCount, Buffer input) {
        ByteBuffer result;
        boolean convertWholeBuffer = byteCount < 0;
        int position;
        int i;
        if (input instanceof ByteBuffer) {
            ByteBuffer input2 = (ByteBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit();
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            position = input2.position();
            for (i = 0; i < byteCount; i++) {
                result.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof CharBuffer) {
            CharBuffer input22 = (CharBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input22.limit() * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input22.order());
            CharBuffer result2 = result.asCharBuffer();
            position = input22.position();
            for (i = 0; i < byteCount / 2; i++) {
                result2.put(input22.get());
            }
            input22.position(position);
        } else if (input instanceof ShortBuffer) {
            ShortBuffer input23 = (ShortBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input23.limit() * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input23.order());
            ShortBuffer result22 = result.asShortBuffer();
            position = input23.position();
            for (i = 0; i < byteCount / 2; i++) {
                result22.put(input23.get());
            }
            input23.position(position);
        } else if (input instanceof IntBuffer) {
            IntBuffer input24 = (IntBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input24.limit() * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input24.order());
            IntBuffer result23 = result.asIntBuffer();
            position = input24.position();
            for (i = 0; i < byteCount / 4; i++) {
                result23.put(input24.get());
            }
            input24.position(position);
        } else if (input instanceof FloatBuffer) {
            FloatBuffer input25 = (FloatBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input25.limit() * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input25.order());
            FloatBuffer result24 = result.asFloatBuffer();
            position = input25.position();
            for (i = 0; i < byteCount / 4; i++) {
                result24.put(input25.get());
            }
            input25.position(position);
        } else if (input instanceof DoubleBuffer) {
            DoubleBuffer input26 = (DoubleBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input26.limit() * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input26.order());
            DoubleBuffer result25 = result.asDoubleBuffer();
            position = input26.position();
            for (i = 0; i < byteCount / 8; i++) {
                result25.put(input26.get());
            }
            input26.position(position);
        } else if (input instanceof LongBuffer) {
            LongBuffer input27 = (LongBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input27.limit() * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input27.order());
            LongBuffer result26 = result.asLongBuffer();
            position = input27.position();
            for (i = 0; i < byteCount / 8; i++) {
                result26.put(input27.get());
            }
            input27.position(position);
        } else {
            throw new RuntimeException("Unimplemented Buffer subclass.");
        }
        result.rewind();
        result.order(ByteOrder.nativeOrder());
        return result;
    }

    private char[] toCharIndices(int count, int type, Buffer indices) {
        char[] result = new char[count];
        switch (type) {
            case 5121:
                ByteBuffer byteBuffer = toByteBuffer(count, indices);
                byte[] array = byteBuffer.array();
                int offset = byteBuffer.arrayOffset();
                for (int i = 0; i < count; i++) {
                    result[i] = (char) (array[offset + i] & 255);
                }
                break;
            case 5123:
                CharBuffer charBuffer;
                if (indices instanceof CharBuffer) {
                    charBuffer = (CharBuffer) indices;
                } else {
                    charBuffer = toByteBuffer(count * 2, indices).asCharBuffer();
                }
                int oldPosition = charBuffer.position();
                charBuffer.position(0);
                charBuffer.get(result);
                charBuffer.position(oldPosition);
                break;
        }
        return result;
    }

    private void doArrayElement(StringBuilder builder, boolean enabled, String name, PointerInfo pointer, int index) {
        if (enabled) {
            builder.append(" ");
            builder.append(new StringBuilder(String.valueOf(name)).append(":{").toString());
            if (pointer == null) {
                builder.append("undefined");
            } else if (pointer.mStride < 0) {
                builder.append("invalid stride");
            } else {
                int stride = pointer.getStride();
                ByteBuffer byteBuffer = pointer.mTempByteBuffer;
                int size = pointer.mSize;
                int type = pointer.mType;
                int sizeofType = pointer.sizeof(type);
                int byteOffset = stride * index;
                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    switch (type) {
                        case 5120:
                            builder.append(Integer.toString(byteBuffer.get(byteOffset)));
                            break;
                        case 5121:
                            builder.append(Integer.toString(byteBuffer.get(byteOffset) & 255));
                            break;
                        case 5122:
                            builder.append(Integer.toString(byteBuffer.asShortBuffer().get(byteOffset / 2)));
                            break;
                        case 5126:
                            builder.append(Float.toString(byteBuffer.asFloatBuffer().get(byteOffset / 4)));
                            break;
                        case GL20.GL_FIXED /*5132*/:
                            builder.append(Integer.toString(byteBuffer.asIntBuffer().get(byteOffset / 4)));
                            break;
                        default:
                            builder.append("?");
                            break;
                    }
                    byteOffset += sizeofType;
                }
                builder.append("}");
            }
        }
    }

    private void doElement(StringBuilder builder, int ordinal, int vertexIndex) {
        builder.append(" [" + ordinal + " : " + vertexIndex + "] =");
        doArrayElement(builder, this.mVertexArrayEnabled, "v", this.mVertexPointer, vertexIndex);
        doArrayElement(builder, this.mNormalArrayEnabled, "n", this.mNormalPointer, vertexIndex);
        doArrayElement(builder, this.mColorArrayEnabled, "c", this.mColorPointer, vertexIndex);
        doArrayElement(builder, this.mTextureCoordArrayEnabled, "t", this.mTexCoordPointer, vertexIndex);
        builder.append("\n");
    }

    private void bindArrays() {
        if (this.mColorArrayEnabled) {
            this.mColorPointer.bindByteBuffer();
        }
        if (this.mNormalArrayEnabled) {
            this.mNormalPointer.bindByteBuffer();
        }
        if (this.mTextureCoordArrayEnabled) {
            this.mTexCoordPointer.bindByteBuffer();
        }
        if (this.mVertexArrayEnabled) {
            this.mVertexPointer.bindByteBuffer();
        }
    }

    private void unbindArrays() {
        if (this.mColorArrayEnabled) {
            this.mColorPointer.unbindByteBuffer();
        }
        if (this.mNormalArrayEnabled) {
            this.mNormalPointer.unbindByteBuffer();
        }
        if (this.mTextureCoordArrayEnabled) {
            this.mTexCoordPointer.unbindByteBuffer();
        }
        if (this.mVertexArrayEnabled) {
            this.mVertexPointer.unbindByteBuffer();
        }
    }

    private void startLogIndices() {
        this.mStringBuilder = new StringBuilder();
        this.mStringBuilder.append("\n");
        bindArrays();
    }

    private void endLogIndices() {
        log(this.mStringBuilder.toString());
        unbindArrays();
    }

    public void glActiveTexture(int texture) {
        begin("glActiveTexture");
        arg("texture", texture);
        end();
        this.mgl.glActiveTexture(texture);
        checkError();
    }

    public void glAlphaFunc(int func, float ref) {
        begin("glAlphaFunc");
        arg("func", func);
        arg("ref", ref);
        end();
        this.mgl.glAlphaFunc(func, ref);
        checkError();
    }

    public void glAlphaFuncx(int func, int ref) {
        begin("glAlphaFuncx");
        arg("func", func);
        arg("ref", ref);
        end();
        this.mgl.glAlphaFuncx(func, ref);
        checkError();
    }

    public void glBindTexture(int target, int texture) {
        begin("glBindTexture");
        arg("target", getTextureTarget(target));
        arg("texture", texture);
        end();
        this.mgl.glBindTexture(target, texture);
        checkError();
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        begin("glBlendFunc");
        arg("sfactor", getFactor(sfactor));
        arg("dfactor", getFactor(dfactor));
        end();
        this.mgl.glBlendFunc(sfactor, dfactor);
        checkError();
    }

    public void glClear(int mask) {
        begin("glClear");
        arg("mask", getClearBufferMask(mask));
        end();
        this.mgl.glClear(mask);
        checkError();
    }

    public void glClearColor(float red, float green, float blue, float alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();
        this.mgl.glClearColor(red, green, blue, alpha);
        checkError();
    }

    public void glClearColorx(int red, int green, int blue, int alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();
        this.mgl.glClearColorx(red, green, blue, alpha);
        checkError();
    }

    public void glClearDepthf(float depth) {
        begin("glClearDepthf");
        arg("depth", depth);
        end();
        this.mgl.glClearDepthf(depth);
        checkError();
    }

    public void glClearDepthx(int depth) {
        begin("glClearDepthx");
        arg("depth", depth);
        end();
        this.mgl.glClearDepthx(depth);
        checkError();
    }

    public void glClearStencil(int s) {
        begin("glClearStencil");
        arg("s", s);
        end();
        this.mgl.glClearStencil(s);
        checkError();
    }

    public void glClientActiveTexture(int texture) {
        begin("glClientActiveTexture");
        arg("texture", texture);
        end();
        this.mgl.glClientActiveTexture(texture);
        checkError();
    }

    public void glColor4f(float red, float green, float blue, float alpha) {
        begin("glColor4f");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();
        this.mgl.glColor4f(red, green, blue, alpha);
        checkError();
    }

    public void glColor4x(int red, int green, int blue, int alpha) {
        begin("glColor4x");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();
        this.mgl.glColor4x(red, green, blue, alpha);
        checkError();
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        begin("glColorMask");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();
        this.mgl.glColorMask(red, green, blue, alpha);
        checkError();
    }

    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        begin("glColorPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mColorPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glColorPointer(size, type, stride, pointer);
        checkError();
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
        begin("glCompressedTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();
        this.mgl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
        checkError();
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
        begin("glCompressedTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();
        this.mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
        checkError();
    }

    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
        begin("glCopyTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        end();
        this.mgl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
        checkError();
    }

    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        begin("glCopyTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
        checkError();
    }

    public void glCullFace(int mode) {
        begin("glCullFace");
        arg("mode", mode);
        end();
        this.mgl.glCullFace(mode);
        checkError();
    }

    public void glDeleteTextures(int n, int[] textures, int offset) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures, offset);
        arg("offset", offset);
        end();
        this.mgl.glDeleteTextures(n, textures, offset);
        checkError();
    }

    public void glDeleteTextures(int n, IntBuffer textures) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures);
        end();
        this.mgl.glDeleteTextures(n, textures);
        checkError();
    }

    public void glDepthFunc(int func) {
        begin("glDepthFunc");
        arg("func", func);
        end();
        this.mgl.glDepthFunc(func);
        checkError();
    }

    public void glDepthMask(boolean flag) {
        begin("glDepthMask");
        arg("flag", flag);
        end();
        this.mgl.glDepthMask(flag);
        checkError();
    }

    public void glDepthRangef(float near, float far) {
        begin("glDepthRangef");
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glDepthRangef(near, far);
        checkError();
    }

    public void glDepthRangex(int near, int far) {
        begin("glDepthRangex");
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glDepthRangex(near, far);
        checkError();
    }

    public void glDisable(int cap) {
        begin("glDisable");
        arg("cap", getCap(cap));
        end();
        this.mgl.glDisable(cap);
        checkError();
    }

    public void glDisableClientState(int array) {
        begin("glDisableClientState");
        arg("array", getClientState(array));
        end();
        switch (array) {
            case GL10.GL_VERTEX_ARRAY /*32884*/:
                this.mVertexArrayEnabled = false;
                break;
            case GL10.GL_NORMAL_ARRAY /*32885*/:
                this.mNormalArrayEnabled = false;
                break;
            case GL10.GL_COLOR_ARRAY /*32886*/:
                this.mColorArrayEnabled = false;
                break;
            case GL10.GL_TEXTURE_COORD_ARRAY /*32888*/:
                this.mTextureCoordArrayEnabled = false;
                break;
        }
        this.mgl.glDisableClientState(array);
        checkError();
    }

    public void glDrawArrays(int mode, int first, int count) {
        begin("glDrawArrays");
        arg("mode", mode);
        arg("first", first);
        arg("count", count);
        startLogIndices();
        for (int i = 0; i < count; i++) {
            doElement(this.mStringBuilder, i, first + i);
        }
        endLogIndices();
        end();
        this.mgl.glDrawArrays(mode, first, count);
        checkError();
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        begin("glDrawElements");
        arg("mode", getBeginMode(mode));
        arg("count", count);
        arg("type", getIndexType(type));
        char[] indexArray = toCharIndices(count, type, indices);
        int indexArrayLength = indexArray.length;
        startLogIndices();
        for (int i = 0; i < indexArrayLength; i++) {
            doElement(this.mStringBuilder, i, indexArray[i]);
        }
        endLogIndices();
        end();
        this.mgl.glDrawElements(mode, count, type, indices);
        checkError();
    }

    public void glEnable(int cap) {
        begin("glEnable");
        arg("cap", getCap(cap));
        end();
        this.mgl.glEnable(cap);
        checkError();
    }

    public void glEnableClientState(int array) {
        begin("glEnableClientState");
        arg("array", getClientState(array));
        end();
        switch (array) {
            case GL10.GL_VERTEX_ARRAY /*32884*/:
                this.mVertexArrayEnabled = true;
                break;
            case GL10.GL_NORMAL_ARRAY /*32885*/:
                this.mNormalArrayEnabled = true;
                break;
            case GL10.GL_COLOR_ARRAY /*32886*/:
                this.mColorArrayEnabled = true;
                break;
            case GL10.GL_TEXTURE_COORD_ARRAY /*32888*/:
                this.mTextureCoordArrayEnabled = true;
                break;
        }
        this.mgl.glEnableClientState(array);
        checkError();
    }

    public void glFinish() {
        begin("glFinish");
        end();
        this.mgl.glFinish();
        checkError();
    }

    public void glFlush() {
        begin("glFlush");
        end();
        this.mgl.glFlush();
        checkError();
    }

    public void glFogf(int pname, float param) {
        begin("glFogf");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl.glFogf(pname, param);
        checkError();
    }

    public void glFogfv(int pname, float[] params, int offset) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glFogfv(pname, params, offset);
        checkError();
    }

    public void glFogfv(int pname, FloatBuffer params) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();
        this.mgl.glFogfv(pname, params);
        checkError();
    }

    public void glFogx(int pname, int param) {
        begin("glFogx");
        arg("pname", getFogPName(pname));
        arg("param", param);
        end();
        this.mgl.glFogx(pname, param);
        checkError();
    }

    public void glFogxv(int pname, int[] params, int offset) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glFogxv(pname, params, offset);
        checkError();
    }

    public void glFogxv(int pname, IntBuffer params) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();
        this.mgl.glFogxv(pname, params);
        checkError();
    }

    public void glFrontFace(int mode) {
        begin("glFrontFace");
        arg("mode", mode);
        end();
        this.mgl.glFrontFace(mode);
        checkError();
    }

    public void glFrustumf(float left, float right, float bottom, float top, float near, float far) {
        begin("glFrustumf");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glFrustumf(left, right, bottom, top, near, far);
        checkError();
    }

    public void glFrustumx(int left, int right, int bottom, int top, int near, int far) {
        begin("glFrustumx");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glFrustumx(left, right, bottom, top, near, far);
        checkError();
    }

    public void glGenTextures(int n, int[] textures, int offset) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", Arrays.toString(textures));
        arg("offset", offset);
        this.mgl.glGenTextures(n, textures, offset);
        returns(toString(n, 0, textures, offset));
        checkError();
    }

    public void glGenTextures(int n, IntBuffer textures) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", textures.toString());
        this.mgl.glGenTextures(n, textures);
        returns(toString(n, 0, textures));
        checkError();
    }

    public int glGetError() {
        begin("glGetError");
        int result = this.mgl.glGetError();
        returns(result);
        return result;
    }

    public void glGetIntegerv(int pname, int[] params, int offset) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", Arrays.toString(params));
        arg("offset", offset);
        this.mgl.glGetIntegerv(pname, params, offset);
        returns(toString(getIntegerStateSize(pname), getIntegerStateFormat(pname), params, offset));
        checkError();
    }

    public void glGetIntegerv(int pname, IntBuffer params) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", params.toString());
        this.mgl.glGetIntegerv(pname, params);
        returns(toString(getIntegerStateSize(pname), getIntegerStateFormat(pname), params));
        checkError();
    }

    public String glGetString(int name) {
        begin("glGetString");
        arg("name", name);
        String result = this.mgl.glGetString(name);
        returns(result);
        checkError();
        return result;
    }

    public void glHint(int target, int mode) {
        begin("glHint");
        arg("target", getHintTarget(target));
        arg("mode", getHintMode(mode));
        end();
        this.mgl.glHint(target, mode);
        checkError();
    }

    public void glLightModelf(int pname, float param) {
        begin("glLightModelf");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightModelf(pname, param);
        checkError();
    }

    public void glLightModelfv(int pname, float[] params, int offset) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glLightModelfv(pname, params, offset);
        checkError();
    }

    public void glLightModelfv(int pname, FloatBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();
        this.mgl.glLightModelfv(pname, params);
        checkError();
    }

    public void glLightModelx(int pname, int param) {
        begin("glLightModelx");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightModelx(pname, param);
        checkError();
    }

    public void glLightModelxv(int pname, int[] params, int offset) {
        begin("glLightModelxv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glLightModelxv(pname, params, offset);
        checkError();
    }

    public void glLightModelxv(int pname, IntBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();
        this.mgl.glLightModelxv(pname, params);
        checkError();
    }

    public void glLightf(int light, int pname, float param) {
        begin("glLightf");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightf(light, pname, param);
        checkError();
    }

    public void glLightfv(int light, int pname, float[] params, int offset) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glLightfv(light, pname, params, offset);
        checkError();
    }

    public void glLightfv(int light, int pname, FloatBuffer params) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();
        this.mgl.glLightfv(light, pname, params);
        checkError();
    }

    public void glLightx(int light, int pname, int param) {
        begin("glLightx");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();
        this.mgl.glLightx(light, pname, param);
        checkError();
    }

    public void glLightxv(int light, int pname, int[] params, int offset) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glLightxv(light, pname, params, offset);
        checkError();
    }

    public void glLightxv(int light, int pname, IntBuffer params) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();
        this.mgl.glLightxv(light, pname, params);
        checkError();
    }

    public void glLineWidth(float width) {
        begin("glLineWidth");
        arg("width", width);
        end();
        this.mgl.glLineWidth(width);
        checkError();
    }

    public void glLineWidthx(int width) {
        begin("glLineWidthx");
        arg("width", width);
        end();
        this.mgl.glLineWidthx(width);
        checkError();
    }

    public void glLoadIdentity() {
        begin("glLoadIdentity");
        end();
        this.mgl.glLoadIdentity();
        checkError();
    }

    public void glLoadMatrixf(float[] m, int offset) {
        begin("glLoadMatrixf");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();
        this.mgl.glLoadMatrixf(m, offset);
        checkError();
    }

    public void glLoadMatrixf(FloatBuffer m) {
        begin("glLoadMatrixf");
        arg("m", 16, m);
        end();
        this.mgl.glLoadMatrixf(m);
        checkError();
    }

    public void glLoadMatrixx(int[] m, int offset) {
        begin("glLoadMatrixx");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();
        this.mgl.glLoadMatrixx(m, offset);
        checkError();
    }

    public void glLoadMatrixx(IntBuffer m) {
        begin("glLoadMatrixx");
        arg("m", 16, m);
        end();
        this.mgl.glLoadMatrixx(m);
        checkError();
    }

    public void glLogicOp(int opcode) {
        begin("glLogicOp");
        arg("opcode", opcode);
        end();
        this.mgl.glLogicOp(opcode);
        checkError();
    }

    public void glMaterialf(int face, int pname, float param) {
        begin("glMaterialf");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();
        this.mgl.glMaterialf(face, pname, param);
        checkError();
    }

    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        begin("glMaterialfv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glMaterialfv(face, pname, params, offset);
        checkError();
    }

    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        begin("glMaterialfv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();
        this.mgl.glMaterialfv(face, pname, params);
        checkError();
    }

    public void glMaterialx(int face, int pname, int param) {
        begin("glMaterialx");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();
        this.mgl.glMaterialx(face, pname, param);
        checkError();
    }

    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        begin("glMaterialxv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glMaterialxv(face, pname, params, offset);
        checkError();
    }

    public void glMaterialxv(int face, int pname, IntBuffer params) {
        begin("glMaterialxv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();
        this.mgl.glMaterialxv(face, pname, params);
        checkError();
    }

    public void glMatrixMode(int mode) {
        begin("glMatrixMode");
        arg("mode", getMatrixMode(mode));
        end();
        this.mgl.glMatrixMode(mode);
        checkError();
    }

    public void glMultMatrixf(float[] m, int offset) {
        begin("glMultMatrixf");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();
        this.mgl.glMultMatrixf(m, offset);
        checkError();
    }

    public void glMultMatrixf(FloatBuffer m) {
        begin("glMultMatrixf");
        arg("m", 16, m);
        end();
        this.mgl.glMultMatrixf(m);
        checkError();
    }

    public void glMultMatrixx(int[] m, int offset) {
        begin("glMultMatrixx");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();
        this.mgl.glMultMatrixx(m, offset);
        checkError();
    }

    public void glMultMatrixx(IntBuffer m) {
        begin("glMultMatrixx");
        arg("m", 16, m);
        end();
        this.mgl.glMultMatrixx(m);
        checkError();
    }

    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        begin("glMultiTexCoord4f");
        arg("target", target);
        arg("s", s);
        arg("t", t);
        arg("r", r);
        arg("q", q);
        end();
        this.mgl.glMultiTexCoord4f(target, s, t, r, q);
        checkError();
    }

    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        begin("glMultiTexCoord4x");
        arg("target", target);
        arg("s", s);
        arg("t", t);
        arg("r", r);
        arg("q", q);
        end();
        this.mgl.glMultiTexCoord4x(target, s, t, r, q);
        checkError();
    }

    public void glNormal3f(float nx, float ny, float nz) {
        begin("glNormal3f");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();
        this.mgl.glNormal3f(nx, ny, nz);
        checkError();
    }

    public void glNormal3x(int nx, int ny, int nz) {
        begin("glNormal3x");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();
        this.mgl.glNormal3x(nx, ny, nz);
        checkError();
    }

    public void glNormalPointer(int type, int stride, Buffer pointer) {
        begin("glNormalPointer");
        arg("type", type);
        arg("stride", stride);
        arg("pointer", pointer.toString());
        end();
        this.mNormalPointer = new PointerInfo(3, type, stride, pointer);
        this.mgl.glNormalPointer(type, stride, pointer);
        checkError();
    }

    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        begin("glOrthof");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glOrthof(left, right, bottom, top, near, far);
        checkError();
    }

    public void glOrthox(int left, int right, int bottom, int top, int near, int far) {
        begin("glOrthox");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();
        this.mgl.glOrthox(left, right, bottom, top, near, far);
        checkError();
    }

    public void glPixelStorei(int pname, int param) {
        begin("glPixelStorei");
        arg("pname", pname);
        arg("param", param);
        end();
        this.mgl.glPixelStorei(pname, param);
        checkError();
    }

    public void glPointSize(float size) {
        begin("glPointSize");
        arg("size", size);
        end();
        this.mgl.glPointSize(size);
        checkError();
    }

    public void glPointSizex(int size) {
        begin("glPointSizex");
        arg("size", size);
        end();
        this.mgl.glPointSizex(size);
        checkError();
    }

    public void glPolygonOffset(float factor, float units) {
        begin("glPolygonOffset");
        arg("factor", factor);
        arg("units", units);
        end();
        this.mgl.glPolygonOffset(factor, units);
        checkError();
    }

    public void glPolygonOffsetx(int factor, int units) {
        begin("glPolygonOffsetx");
        arg("factor", factor);
        arg("units", units);
        end();
        this.mgl.glPolygonOffsetx(factor, units);
        checkError();
    }

    public void glPopMatrix() {
        begin("glPopMatrix");
        end();
        this.mgl.glPopMatrix();
        checkError();
    }

    public void glPushMatrix() {
        begin("glPushMatrix");
        end();
        this.mgl.glPushMatrix();
        checkError();
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        begin("glReadPixels");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    public void glRotatef(float angle, float x, float y, float z) {
        begin("glRotatef");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glRotatef(angle, x, y, z);
        checkError();
    }

    public void glRotatex(int angle, int x, int y, int z) {
        begin("glRotatex");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glRotatex(angle, x, y, z);
        checkError();
    }

    public void glSampleCoverage(float value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();
        this.mgl.glSampleCoverage(value, invert);
        checkError();
    }

    public void glSampleCoveragex(int value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();
        this.mgl.glSampleCoveragex(value, invert);
        checkError();
    }

    public void glScalef(float x, float y, float z) {
        begin("glScalef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glScalef(x, y, z);
        checkError();
    }

    public void glScalex(int x, int y, int z) {
        begin("glScalex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glScalex(x, y, z);
        checkError();
    }

    public void glScissor(int x, int y, int width, int height) {
        begin("glScissor");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glScissor(x, y, width, height);
        checkError();
    }

    public void glShadeModel(int mode) {
        begin("glShadeModel");
        arg("mode", getShadeModel(mode));
        end();
        this.mgl.glShadeModel(mode);
        checkError();
    }

    public void glStencilFunc(int func, int ref, int mask) {
        begin("glStencilFunc");
        arg("func", func);
        arg("ref", ref);
        arg("mask", mask);
        end();
        this.mgl.glStencilFunc(func, ref, mask);
        checkError();
    }

    public void glStencilMask(int mask) {
        begin("glStencilMask");
        arg("mask", mask);
        end();
        this.mgl.glStencilMask(mask);
        checkError();
    }

    public void glStencilOp(int fail, int zfail, int zpass) {
        begin("glStencilOp");
        arg("fail", fail);
        arg("zfail", zfail);
        arg("zpass", zpass);
        end();
        this.mgl.glStencilOp(fail, zfail, zpass);
        checkError();
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        begin("glTexCoordPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mTexCoordPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glTexCoordPointer(size, type, stride, pointer);
        checkError();
    }

    public void glTexEnvf(int target, int pname, float param) {
        begin("glTexEnvf");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", getTextureEnvParamName(param));
        end();
        this.mgl.glTexEnvf(target, pname, param);
        checkError();
    }

    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glTexEnvfv(target, pname, params, offset);
        checkError();
    }

    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();
        this.mgl.glTexEnvfv(target, pname, params);
        checkError();
    }

    public void glTexEnvx(int target, int pname, int param) {
        begin("glTexEnvx");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", param);
        end();
        this.mgl.glTexEnvx(target, pname, param);
        checkError();
    }

    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg("offset", offset);
        end();
        this.mgl.glTexEnvxv(target, pname, params, offset);
        checkError();
    }

    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();
        this.mgl.glTexEnvxv(target, pname, params);
        checkError();
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        begin("glTexImage2D");
        arg("target", target);
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
        checkError();
    }

    public void glTexParameterf(int target, int pname, float param) {
        begin("glTexParameterf");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", getTextureParamName(param));
        end();
        this.mgl.glTexParameterf(target, pname, param);
        checkError();
    }

    public void glTexParameterx(int target, int pname, int param) {
        begin("glTexParameterx");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", param);
        end();
        this.mgl.glTexParameterx(target, pname, param);
        checkError();
    }

    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params, offset);
        end();
        this.mgl11.glTexParameteriv(target, pname, params, offset);
        checkError();
    }

    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params);
        end();
        this.mgl11.glTexParameteriv(target, pname, params);
        checkError();
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        begin("glTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        this.mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        checkError();
    }

    public void glTranslatef(float x, float y, float z) {
        begin("glTranslatef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glTranslatef(x, y, z);
        checkError();
    }

    public void glTranslatex(int x, int y, int z) {
        begin("glTranslatex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        this.mgl.glTranslatex(x, y, z);
        checkError();
    }

    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        begin("glVertexPointer");
        argPointer(size, type, stride, pointer);
        end();
        this.mVertexPointer = new PointerInfo(size, type, stride, pointer);
        this.mgl.glVertexPointer(size, type, stride, pointer);
        checkError();
    }

    public void glViewport(int x, int y, int width, int height) {
        begin("glViewport");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl.glViewport(x, y, width, height);
        checkError();
    }

    public void glClipPlanef(int plane, float[] equation, int offset) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg("offset", offset);
        end();
        this.mgl11.glClipPlanef(plane, equation, offset);
        checkError();
    }

    public void glClipPlanef(int plane, FloatBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        this.mgl11.glClipPlanef(plane, equation);
        checkError();
    }

    public void glClipPlanex(int plane, int[] equation, int offset) {
        begin("glClipPlanex");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg("offset", offset);
        end();
        this.mgl11.glClipPlanex(plane, equation, offset);
        checkError();
    }

    public void glClipPlanex(int plane, IntBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        this.mgl11.glClipPlanex(plane, equation);
        checkError();
    }

    public void glDrawTexfOES(float x, float y, float z, float width, float height) {
        begin("glDrawTexfOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexfOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexfvOES(float[] coords, int offset) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        this.mgl11Ext.glDrawTexfvOES(coords, offset);
        checkError();
    }

    public void glDrawTexfvOES(FloatBuffer coords) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexfvOES(coords);
        checkError();
    }

    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexiOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexiOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexivOES(int[] coords, int offset) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        this.mgl11Ext.glDrawTexivOES(coords, offset);
        checkError();
    }

    public void glDrawTexivOES(IntBuffer coords) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexivOES(coords);
        checkError();
    }

    public void glDrawTexsOES(short x, short y, short z, short width, short height) {
        begin("glDrawTexsOES");
        arg("x", (int) x);
        arg("y", (int) y);
        arg("z", (int) z);
        arg("width", (int) width);
        arg("height", (int) height);
        end();
        this.mgl11Ext.glDrawTexsOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexsvOES(short[] coords, int offset) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        this.mgl11Ext.glDrawTexsvOES(coords, offset);
        checkError();
    }

    public void glDrawTexsvOES(ShortBuffer coords) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexsvOES(coords);
        checkError();
    }

    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexxOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        this.mgl11Ext.glDrawTexxOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexxvOES(int[] coords, int offset) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        this.mgl11Ext.glDrawTexxvOES(coords, offset);
        checkError();
    }

    public void glDrawTexxvOES(IntBuffer coords) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords);
        end();
        this.mgl11Ext.glDrawTexxvOES(coords);
        checkError();
    }

    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset, int[] exponent, int exponentOffset) {
        begin("glQueryMatrixxOES");
        arg("mantissa", Arrays.toString(mantissa));
        arg("exponent", Arrays.toString(exponent));
        end();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset, exponent, exponentOffset);
        returns(toString(16, 2, mantissa, mantissaOffset));
        returns(toString(16, 0, exponent, exponentOffset));
        checkError();
        return valid;
    }

    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        begin("glQueryMatrixxOES");
        arg("mantissa", mantissa.toString());
        arg("exponent", exponent.toString());
        end();
        int valid = this.mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
        returns(toString(16, 2, mantissa));
        returns(toString(16, 0, exponent));
        checkError();
        return valid;
    }

    public void glBindBuffer(int target, int buffer) {
        throw new UnsupportedOperationException();
    }

    public void glBufferData(int target, int size, Buffer data, int usage) {
        throw new UnsupportedOperationException();
    }

    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        throw new UnsupportedOperationException();
    }

    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        throw new UnsupportedOperationException();
    }

    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glDeleteBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    public void glGenBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGenBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetBooleanv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    public void glGetFixedv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetFixedv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetFloatv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetFloatv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightxv(int light, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsBuffer(int buffer) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsEnabled(int cap) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsTexture(int texture) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterf(int pname, float param) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterfv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterfv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterx(int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterxv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterxv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnvi(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnviv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameteri(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }
}
