package com.farah.foodapp.algorithm;

import android.content.Context;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SentimentInterpreter {

    private static Interpreter interpreter = null; // still the model is no loaded

    private static MappedByteBuffer loadModel(Context context) throws IOException {
        FileInputStream fis = new FileInputStream(context.getAssets().openFd("sentiment_model_v3.tflite").getFileDescriptor());
        FileChannel channel = fis.getChannel();
        long startOffset = context.getAssets().openFd("sentiment_model_v3.tflite").getStartOffset();
        long declaredLength = context.getAssets().openFd("sentiment_model_v3.tflite").getDeclaredLength();
        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    private static void init(Context context) {
        if (interpreter == null) {// inter is used to read the tflite model
            try {
                interpreter = new Interpreter(loadModel(context));// to make sure model is loaded
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
// this code is called in the comments dialog to reach model

    public static float[] predict(float[] inputVector, Context context) {

        init(context);// this is used to reach the pre-trained model in assets and load it

        float[][] output = new float[1][3]; // one comment 3 feelings output = [neg, neu, pos]


        interpreter.run(inputVector, output); /* take the given vec(comment)
        decide how many each feeling represent */

        return output[0]; // this is where the feelings are for each single comment
        // the first element of the batch has the feelings
    }
}
