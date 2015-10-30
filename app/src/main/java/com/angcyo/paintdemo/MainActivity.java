package com.angcyo.paintdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.angcyo.paintdemo.Util.Util;
import com.angcyo.paintdemo.paint.PaintConfig;
import com.angcyo.paintdemo.paint.PaintView;
import com.angcyo.paintdemo.socket.ServerSocket;
import com.angcyo.paintdemo.socket.SocketConfig;

import java.io.File;

import nt.finger.paint.FingerPaint;

public class MainActivity extends AppCompatActivity {

    public static final int GUIDE_WIN_OFFSET = 60;
    public static LocalBroadcastManager localBroadcastManager;
    boolean isShare = false;
    private RadioGroup shapeGroup;
    private Button btUndo, btColor, btClear, btWidth, btCapture, btShare;
    private PaintView paintView;
    private PopupWindow mColPopup, mWidthPopup, mGuideServer, mGuideClient, mPopupTips;
    private View rootView;
    private String localIp;
    private ServerSocket mServerSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, FingerPaint.class));
            }
        });

        initView();
        initEvent();
        initData();
        initBroadcast();
        initShow();
    }

    private void initShow() {
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGuideServer.showAtLocation(rootView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, GUIDE_WIN_OFFSET);
//                showPopupTip("Hello Angcyo");
            }
        }, 1000);
    }

    private void showPopupTip(String tip) {
        TextView tipView = ((TextView) mPopupTips.getContentView().findViewById(R.id.txTips));
        tipView.setText(tip + "");
        mPopupTips.showAtLocation(rootView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, GUIDE_WIN_OFFSET);
        tipView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPopupTips != null && mPopupTips.isShowing()) {
                    mPopupTips.dismiss();
                }
            }
        }, 1000);
    }

    private void initBroadcast() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(PaintView.BDC_CAPTURE);//监听截图成功的广播
        intentFilter.addAction(SocketConfig.BDC_CONNECT_CLIENT);
        intentFilter.addAction(SocketConfig.BDC_CONNECT_SERVER);

        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Bundle bundle = intent.getExtras();
                if (intent.getAction().equalsIgnoreCase(PaintView.BDC_CAPTURE)) {//截图广播
                    if (bundle.getInt(PaintView.CAPTURE_CODE) == 1) {
                        if (isShare) {
                            share(bundle.getString(PaintView.CAPTURE_PATH));
                            isShare = false;
                        } else {
                            Toast.makeText(MainActivity.this, "保存至:" + bundle.getString(PaintView.CAPTURE_PATH), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "截图失败", Toast.LENGTH_LONG).show();
                    }
                }
                if (intent.getAction().equalsIgnoreCase(SocketConfig.BDC_CONNECT_CLIENT)) {//客户端连接广播
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showPopupTip("客户端:" + bundle.getString(SocketConfig.KEY_CLIENT_IP) + "已连接");
                        }
                    });
                }
                if (intent.getAction().equalsIgnoreCase(SocketConfig.BDC_CONNECT_SERVER)) {//连上服务端广播
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showPopupTip("已连上:" + bundle.getString(SocketConfig.KEY_SERVER_IP) + " 服务端");
                        }
                    });
                }
            }
        }, intentFilter);
    }

    private void share(String file) {
        isShare = true;
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (file != null) {
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(file)));
        } else {
            intent.setType("text/plain");
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, "快快来围观");
        intent.putExtra(Intent.EXTRA_TEXT, "为何可以这么美");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享至"));
    }

    private void initData() {
        switch (getIndex((RadioGroup) mColPopup.getContentView().findViewById(R.id.colRG))) {
            case 1:
                PaintConfig.getInstance().setCurrentShapeColor(getResources().getColor(R.color.col2));
                break;
            case 2:
                PaintConfig.getInstance().setCurrentShapeColor(getResources().getColor(R.color.col3));
                break;
            case 3:
                PaintConfig.getInstance().setCurrentShapeColor(getResources().getColor(R.color.col4));
                break;
            case 4:
                PaintConfig.getInstance().setCurrentShapeColor(getResources().getColor(R.color.col5));
                break;
            case 0:
            default:
                PaintConfig.getInstance().setCurrentShapeColor(getResources().getColor(R.color.col1));
                break;
        }
        switch (getIndex((RadioGroup) mWidthPopup.getContentView().findViewById(R.id.widthRG))) {
            case 1:
                PaintConfig.getInstance().setCurrentShapeWidth(getResources().getDimension(R.dimen.width2));
                break;
            case 2:
                PaintConfig.getInstance().setCurrentShapeWidth(getResources().getDimension(R.dimen.width3));
                break;
            case 3:
                PaintConfig.getInstance().setCurrentShapeWidth(getResources().getDimension(R.dimen.width4));
                break;
            case 4:
                PaintConfig.getInstance().setCurrentShapeWidth(getResources().getDimension(R.dimen.width5));
                break;
            case 0:
            default:
                PaintConfig.getInstance().setCurrentShapeWidth(getResources().getDimension(R.dimen.width1));
                break;
        }

        localIp = Util.getIp(this);
        ((TextView) mGuideServer.getContentView().findViewById(R.id.tips)).setText("本机IP:" + localIp);
    }

    private void initEvent() {
        shapeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.line:
                        PaintConfig.getInstance().setCurrentShape(PaintConfig.Shape.Line);
                        break;
                    case R.id.circle:
                        PaintConfig.getInstance().setCurrentShape(PaintConfig.Shape.Circle);
                        break;
                    case R.id.rect:
                        PaintConfig.getInstance().setCurrentShape(PaintConfig.Shape.Rect);
                        break;
                    case R.id.point:
                    default:
                        PaintConfig.getInstance().setCurrentShape(PaintConfig.Shape.Point);
                        break;
                }
            }
        });

        btUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.removeLastOne();
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.removeAll();
            }
        });
        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.capture();
            }
        });
        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.capture();
                isShare = true;
            }
        });

        btColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColPopup.showAsDropDown(btColor, btColor.getWidth(), -btColor.getHeight() - 6);
            }
        });

        btWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWidthPopup.showAsDropDown(btWidth, btWidth.getWidth(), -btWidth.getHeight() - 6);
            }
        });

        mColPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                initData();
            }
        });
        mWidthPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                initData();
            }
        });

        //作为服务端
        mGuideServer.getContentView().findViewById(R.id.btServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuideServer.dismiss();
                SocketConfig.isServer = true;
                SocketConfig.SVR_IP = localIp;
                mServerSocket = new ServerSocket();
                new Thread(mServerSocket).start();
                showPopupTip("等待连接...");
            }
        });

        //作为客户端
        mGuideServer.getContentView().findViewById(R.id.btClient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuideServer.dismiss();
                SocketConfig.isServer = false;
                mGuideClient.showAtLocation(rootView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, GUIDE_WIN_OFFSET);
            }
        });

        //客户端输入服务器IP
        final EditText etServer = (EditText) mGuideClient.getContentView().findViewById(R.id.etServer);
        mGuideClient.getContentView().findViewById(R.id.btSaveIp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String svrIp = etServer.getText().toString();
                if (TextUtils.isEmpty(svrIp)) {
                    etServer.setError("请输入有效值");
                    etServer.requestFocus();
                    return;
                }
                mGuideClient.dismiss();
                SocketConfig.SVR_IP = svrIp;
//                SocketConfig.isStartClient = true;
                paintView.connectServer();
            }
        });
    }

    private void initView() {
        rootView = findViewById(R.id.rootView);
        shapeGroup = (RadioGroup) findViewById(R.id.shape_group);
        btUndo = (Button) findViewById(R.id.cancel);
        btClear = (Button) findViewById(R.id.clear);
        btColor = (Button) findViewById(R.id.color);
        btWidth = (Button) findViewById(R.id.width);
        btCapture = (Button) findViewById(R.id.capture);
        btShare = (Button) findViewById(R.id.share);
        paintView = (PaintView) findViewById(R.id.paint_view);

        //颜色选择弹窗
        LayoutInflater inflater = LayoutInflater.from(this);
        View popColLayout = inflater.inflate(R.layout.layout_col_popup, null);
        mColPopup = new PopupWindow(popColLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mColPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//不设置背景,无法响应 返回键 和 界面外点击事件;应该算是谷歌大神的BUG

        //粗细选择弹窗
        View popWidthLayout = inflater.inflate(R.layout.layout_width_popup, null);
        mWidthPopup = new PopupWindow(popWidthLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mWidthPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //服务端向导
        View guideServer = inflater.inflate(R.layout.guide_server_layout, null);
        mGuideServer = new PopupWindow(guideServer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mGuideServer.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mGuideServer.setAnimationStyle(R.style.GuideWindowAnimation);

        //客户端向导
        View guideClient = inflater.inflate(R.layout.guide_client_layout, null);
        mGuideClient = new PopupWindow(guideClient, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mGuideClient.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mGuideClient.setAnimationStyle(R.style.GuideWindowAnimation);

        //弹窗提示
        View popupTip = inflater.inflate(R.layout.layout_popup_tip, null);
        mPopupTips = new PopupWindow(popupTip, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupTips.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupTips.setAnimationStyle(R.style.GuideWindowAnimation);
        mPopupTips.setTouchable(false);
        mPopupTips.setOutsideTouchable(false);
    }

    private int getIndex(RadioGroup radioGroup) {
        return radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
    }

    private void setIndex(RadioGroup radioGroup, int index) {
        radioGroup.check(radioGroup.getChildAt(index).getId());
    }


    @Override
    protected void onDestroy() {
        if (mServerSocket != null) {
            mServerSocket.exit();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
