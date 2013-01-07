package com.example.scanwifi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class ShowRSSI extends Activity{

	public StartScan scanObject;
	public WriteFile writeFile;
	public WriteFile recordFile;
	
	Thread mythread = new Thread(new myThread());
	int speed = 2000;
	String strBSSID;
	String strSSID;
	int msg = 0;
	String tag = "one";
	
	TextView tv;
	ScrollView scrollView;
	Button stop;
	Button recordAP;
	Button gotoMain;
	SeekBar seekBar;
	EditText et;
	Boolean isStop = false;
	InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oneap);

		TextView nameText = (TextView)findViewById(R.id.textView1);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

		
		tv = (TextView)findViewById(R.id.textView2);
//		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		scrollView = (ScrollView)findViewById(R.id.scrollView1);
		
		stop = (Button)findViewById(R.id.button1);
		recordAP = (Button)findViewById(R.id.button2);
		gotoMain = (Button)findViewById(R.id.button3);
		
		seekBar = (SeekBar)findViewById(R.id.seekBar1);
	
		et = (EditText)findViewById(R.id.editText1);
		et.setVisibility(View.INVISIBLE);//����
		stop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				if(isStop == false){
					stop.setText("get");
					mythread.interrupt();//�ж��̣߳�����һֱ����
					isStop = true;
				}
				else{
					stop.setText("stop");
					mythread = new Thread(new myThread());
					mythread.start();
					isStop = false;
				}
			}
		});
		
		recordAP.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mythread.isInterrupted() == false){
					stop.setText("get");
					mythread.interrupt();
					isStop = true;
				}
				
				et.setVisibility(View.VISIBLE);
				et.setText(""); 
				et.clearComposingText();
				recordFile = new WriteFile(Environment.getExternalStorageDirectory().getPath(),  "recordAP.txt");
			}
			
		});
		
		gotoMain.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();

				intent.setClass(ShowRSSI.this, MainActivity.class);
	         
				ShowRSSI.this.startActivity(intent);
				ShowRSSI.this.finish();
			}
			
		});
		
		et.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if(arg1 == EditorInfo.IME_ACTION_UNSPECIFIED){
					String apPosition = et.getText().toString();
					try {
						recordFile.write(strSSID + "(" + strBSSID + ")" + ":" + apPosition + "\r\n");
						Log.v(tag, String.valueOf(arg1));
						et.setVisibility(View.INVISIBLE);
						imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}
			
		});

		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				int i = arg0.getProgress();
				Log.v(tag, String.valueOf(i));
				speed = (arg0.getMax()-i+1)*2000/arg0.getMax();
				
				try {
					writeFile.write("\r\nSpeed:"+String.valueOf(speed)+"\r\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });
		
		//==============================
		Intent intent = getIntent();
		msg = intent.getIntExtra("msg", 1);
		if(msg == 2){
			strSSID = intent.getStringExtra("SSID");
			strBSSID = intent.getStringExtra("BSSID");
			nameText.setText(strSSID+"\r\n"+strBSSID);
			
			writeFile = new WriteFile(Environment.getExternalStorageDirectory().getPath(), strSSID + ".txt");
		}
		else{
			nameText.setText("ALL AP");
			writeFile = new WriteFile(Environment.getExternalStorageDirectory().getPath(),  "ALLAP.txt");
		}
		
		try {
			writeFile.writeTime();
			writeFile.write("\r\nSpeed:"+String.valueOf(speed)+"\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
		String connectivity_context = Context.WIFI_SERVICE;
		WifiManager wifi = (WifiManager)getSystemService(connectivity_context);
		scanObject = new StartScan(wifi);
		
		mythread.start();
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
            switch(msg.what){    
            case 1:
            	List<ScanResult> result = scanObject.scan();
	        	if(result.isEmpty()){
	        		tv.append("û�м�⵽AP");
	        	}
	        	else{
	        		Iterator<ScanResult> iscan = result.iterator();
	        		while (iscan.hasNext()) {
	        			ScanResult next = iscan.next();
	        			tv.append(next.SSID + " "+ next.BSSID + " " + String.valueOf(next.level)+"\r\n");
	        			try {
							writeFile.write(next.SSID + " "+ next.BSSID + " " + String.valueOf(next.level)+"\r\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		}
	        		
	        		tv.append("\r\n");
	        		tv.append("\r\n");
	        		try {
						writeFile.write("\r\n\r\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        	scroll2Bottom(scrollView,tv);
	        	break;
            	
            case 2: 
            	int i = scanObject.getOnRSSI(strBSSID);
            	tv.append(String.valueOf(i)+"\r\n");
            	try {
					writeFile.write(String.valueOf(i)+"\r\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	scroll2Bottom(scrollView,tv);
            	break;
            	
        	default:
        		break;
            }
            
            super.handleMessage(msg);
        }
    };
	
	class myThread implements Runnable {     
        public void run() {    
             while (!Thread.currentThread().isInterrupted()) {      
                       
                  Message message = new Message();     
                  message.what = msg;     
                      
                  handler.sendMessage(message);     
                  try {     
                       Thread.sleep(speed);      
                  } catch (InterruptedException e) {     
                       Thread.currentThread().interrupt();     
                  }     
             }     
        }     
    }
	
	public void scroll2Bottom(final ScrollView scroll, final View inner) { 
		
        Handler handler = new Handler();  
        handler.post(new Runnable() {  
  
            @Override  
            public void run() {  
                // TODO Auto-generated method stub   
                if (scroll == null || inner == null) {  
                    return;  
                }  
                // �ڲ�߶ȳ������   
                int offset = inner.getMeasuredHeight() + 1
                        - scroll.getMeasuredHeight();  
                if (offset < 0) {  
                    System.out.println("��λ...");  
                    offset = 0;  
                }  
                scroll.scrollTo(0, offset);  
            }  
        });  
    }
}
