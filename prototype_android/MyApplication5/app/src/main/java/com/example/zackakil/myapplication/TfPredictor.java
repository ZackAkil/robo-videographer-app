/**
 * Created by zackakil on 31/12/2017.
 * Class for managing the tensorflow predictor
 */
package com.example.zackakil.myapplication;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TfPredictor {


    private TensorFlowInferenceInterface tfHelper;
    private String inputTensorName;
    private String[] outputTensorNames;
    private float[] predictOutput = new float[1];
    private int[] inputDims = new int[3];

    TfPredictor(String modelAssetName,
                AssetManager assetManager,
                String inputTensorName,
                String outputTensorName,
                int[] inputDims){

        this.tfHelper = new TensorFlowInferenceInterface(assetManager, modelAssetName);
        this.outputTensorNames = new String[]{outputTensorName};
        this.inputTensorName = inputTensorName;
        this.inputDims = inputDims.clone();

    }

    public float predict(Bitmap inputFrame){

        throw (new UnsupportedOperationException());

    }

    public float predict(float[] inputArray){

        tfHelper.feed(this.inputTensorName, inputArray, 1, inputDims[0], inputDims[1],inputDims[2]);

        tfHelper.run(this.outputTensorNames);

        tfHelper.fetch(this.outputTensorNames[0], predictOutput);

        return predictOutput[0];
    }

}
