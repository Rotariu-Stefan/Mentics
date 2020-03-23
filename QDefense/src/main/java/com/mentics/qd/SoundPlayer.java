package com.mentics.qd;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALConstants;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;


public class SoundPlayer {
    private AL al;

    // Sources are points emitting sound.
    private int[] source = new int[1]; // One source for now

    // Position of the listener.
    private float[] listenerPos = { 0.0f, 0.0f, 0.0f };
    // Velocity of the listener.
    private float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    // Orientation of the listener. (first 3 elems are "at", second 3 are "up")
    private float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

    private boolean initialized = false;
    private Map<String, Integer> buffers = new HashMap<>();

    public SoundPlayer() {
        if (!initialize()) {
            System.out.println("There have been an error");
        }
    }

    public void playStaticSound(String resource, float[] position) {
        int buffer = buffers.get(resource);

        setListenerValues();
        setSourceParameters(0, position, new float[3], buffer);

        al.alSourcePlay(source[0]);
    }

    private void setSourceParameters(int index, float[] pos, float[] vel, int buffer) {
        al.alSourcei(source[0], ALConstants.AL_BUFFER, buffer);
        al.alSourcef(source[0], ALConstants.AL_PITCH, 1.0f);
        al.alSourcef(source[0], ALConstants.AL_GAIN, 1.0f);
        al.alSourcei(source[0], ALConstants.AL_LOOPING, ALConstants.AL_FALSE);

        al.alSourcefv(source[index], ALConstants.AL_POSITION, pos, 0);
        al.alSourcefv(source[index], ALConstants.AL_VELOCITY, vel, 0);
    }

    private void setListenerValues() {
        al.alListenerfv(ALConstants.AL_POSITION, listenerPos, 0);
        al.alListenerfv(ALConstants.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(ALConstants.AL_ORIENTATION, listenerOri, 0);
    }

    private boolean initialize() {
        if (initialized) {
            return true;
        }
        // Initialize OpenAL and clear the error bit.
        try {
            ALut.alutInit();
            al = ALFactory.getAL();
            al.alGetError();
        } catch (ALException e) {
            e.printStackTrace();
            return false;
        }

        al.alGenSources(1, source, 0);
        int error = al.alGetError();
        if (error != ALConstants.AL_NO_ERROR) {
            System.out.println("Error: " + getALErrorString(error));
            throw new ALException("Error generating OpenAL source");
        }

        loadFiles();

        initialized = true;
        return true;
    }

    private void loadFiles() {
        loadWAVFile("/com/mentics/qd/res/gun_fire_mono.wav");
    }

    public int loadWAVFile(String resourceFilePath) {
        // variables to load into
        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];
        int[] buffer = new int[1];

        int error;

        // Load wav data into a buffer.
        al.alGenBuffers(1, buffer, 0);
        if (al.alGetError() != ALConstants.AL_NO_ERROR) throw new ALException("Error generating OpenAL buffers");

        ALut.alutLoadWAVFile(QuipNebula.class.getResourceAsStream(resourceFilePath), format, data, size, freq, loop);
        if (data[0] == null) {
            throw new RuntimeException("Error loading WAV file");
        }

        System.out.println("sound size = " + size[0]);
        System.out.println("sound freq = " + freq[0]);

        System.out.println("buffer: " + buffer[0] + "\n" + "format: " + format[0] + "\n");
        al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);
        error = al.alGetError();
        if (error != ALConstants.AL_NO_ERROR) {
            System.out.println("Error in alBufferData: " + getALErrorString(error));
            throw new RuntimeException("Error buffering data ");
        }
        // Do another error check
        if (al.alGetError() != ALConstants.AL_NO_ERROR) throw new ALException("Error setting up OpenAL source");

        buffers.put(resourceFilePath, buffer[0]);
        return ALConstants.AL_TRUE;
    }

    public void killAllData() {
        Collection<Integer> buffersValues = buffers.values();
        int size = buffersValues.size();
        int[] bufferArray = new int[size];
        int i = 0;
        for (Integer buffer : buffersValues) {
            bufferArray[i] = buffer;
            i++;
        }
        al.alDeleteBuffers(size, bufferArray, 0);
        al.alDeleteSources(1, source, 0);
    }

    public static String getALErrorString(int err) {
        switch (err) {
        case ALConstants.AL_NO_ERROR:
            return "AL_NO_ERROR";
        case ALConstants.AL_INVALID_NAME:
            return "AL_INVALID_NAME";
        case ALConstants.AL_INVALID_ENUM:
            return "AL_INVALID_ENUM";
        case ALConstants.AL_INVALID_VALUE:
            return "AL_INVALID_VALUE";
        case ALConstants.AL_INVALID_OPERATION:
            return "AL_INVALID_OPERATION";
        case ALConstants.AL_OUT_OF_MEMORY:
            return "AL_OUT_OF_MEMORY";
        default:
            return "No such error code";
        }
    }
}
