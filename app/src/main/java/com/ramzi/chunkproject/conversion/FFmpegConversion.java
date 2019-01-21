package com.ramzi.chunkproject.conversion;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.ramzi.chunkproject.ChunkMainActivity;
import com.ramzi.chunkproject.R;
import com.ramzi.chunkproject.conversion.interfaces.ConversionCallback;
import com.ramzi.chunkproject.utils.HelperUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliveboard on 21/1/19.
 *
 * @auther Ramesh M Nair
 */
public class FFmpegConversion {
    public ConversionCallback conversionCallback;

    public static final String TAG = "FFmpegConversion";
    Context context;
    List<String> commandList = new LinkedList<>();

    String input;
    int totalPart;
    String destinationDirectory;


    public FFmpegConversion(ConversionCallback conversionCallback, Context context,String input,long videoSize,String destinationDirectory)
    {
        this.conversionCallback=conversionCallback;
        this.context=context;
        this.input=input;
        totalPart = (int) Math.round(videoSize/HelperUtils.SECOUND_TO_SPLIT);
        this.destinationDirectory=destinationDirectory;


        Log.d(TAG,"Constuctor input :"+input+"\n"+"totalPart:"+totalPart+"\n destinationDirectory:"+destinationDirectory);

    }


    public void spliteTimeAndStart(int part,long startTime)
    {


        commandList.clear();
        commandList.add("-i");
        commandList.add(input);
        commandList.add("-acodec");
        commandList.add("copy");
        commandList.add("-vcodec");
        commandList.add("copy");
        commandList.add("-ss");
        commandList.add(HelperUtils.getStartTimeStamp(startTime));
        commandList.add("-t");
        commandList.add(HelperUtils.SECOUND_TO_SPLIT_TIMESTAMP);
        commandList.add(destinationDirectory+"/"+part+HelperUtils.getFileExtention(input));
        String[] command  = commandList.toArray(new String[commandList.size()]);
        if (command.length != 0) {
            Log.d(TAG, "comment " + command);

            execFFmpegBinary(command,part,totalPart,startTime);
        } else {
//            Toast.makeText(ChunkMainActivity.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
        }

    }



    private void execFFmpegBinary(final String[] command, final int part, final int totalPart, final long lastStartTime) {
        try {
            FFmpeg.getInstance(context).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {

                    if(conversionCallback!=null)
                    {
                        conversionCallback.conversionStatus(part+"/"+totalPart+" "+"FAILED with output : " + s);
                    }
                }

                @Override
                public void onSuccess(String s) {

                    if(conversionCallback!=null)
                    {
                        conversionCallback.conversionStatus(part+"/"+totalPart+" "+"SUCCESS with output : " + s);
                    }

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    addTextViewToLayout("progress : "+s);
//                    statusTextView.setText("Processing\n" + s);
                    if(conversionCallback!=null)
                    {
                        conversionCallback.conversionStatus(part+"/"+totalPart+" "+"Processing\n" + s);
                    }
                }

                @Override
                public void onStart() {
//                    outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    if(conversionCallback!=null)
                    {
                        conversionCallback.conversionStatus(part+"/"+totalPart+" "+"Processing...");
                    }
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    progressDialog.dismiss();
                    if(conversionCallback!=null)
                    {
//                        conversionCallback.conversionStatus("Completed,Going for encryption");
                        if(part<totalPart) {
                            spliteTimeAndStart((part + 1),(lastStartTime+HelperUtils.SECOUND_TO_SPLIT));
                        }
                        else
                        {
                            conversionCallback.conversionStatus("Completed,Going for encryption");

                        }
                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }
}
