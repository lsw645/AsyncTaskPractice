package com.lxw.asynctaskpractice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lxw.asynctask.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        AsyncTask<Integer,String> asyncTask = new AsyncTask<Integer,String>() {
//            @Override
//            public String doInBackGround(Integer... integers) {
//                return null;
//            }
//
//            @Override
//            public void postResult(String s) {
//
//            }
//
//            @Override
//            public void onPreExecute() {
//
//            }
//        };
        AsyncTask<String,Integer> asyncTask=new AsyncTask<String, Integer>() {
            @Override
            public Integer doInBackGround(String... strings) {
                Log.d(TAG, "doInBackGround() returned: " + strings[0]);
                return 3;
            }

            @Override
            public void postResult(Integer integer) {
                Log.d(TAG, "postResult() returned: " +integer );
            }

            @Override
            public void onPreExecute() {
                Log.d(TAG, "onPreExecute() returned: " );
            }
        };
        asyncTask.execute("hahhaha");

    }
}
