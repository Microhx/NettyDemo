package ifreecomm.nettydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.littlegreens.netty.client.listener.MessageStateListener;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.littlegreens.netty.client.NettyTcpClient;
import com.littlegreens.netty.client.protobuf.FollowersPlus;
import com.littlegreens.netty.client.status.ConnectState;

import java.util.UUID;

import ifreecomm.nettydemo.adapter.LogAdapter;
import ifreecomm.nettydemo.bean.LogBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NettyClientListener<FollowersPlus.PBMessage> {

    private static final String TAG = "MainActivity";
    private Button mClearLog;
    private Button mSendBtn;
    private Button mConnect;
    private EditText mSendET;
    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private LogAdapter mSendLogAdapter = new LogAdapter();
    private LogAdapter mReceLogAdapter = new LogAdapter();
    private NettyTcpClient mNettyTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initView();

        mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost(Const.HOST)    //设置服务端地址
                .setTcpPort(Const.TCP_PORT) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(false) //设置是否发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData(new byte[]{0x03, 0x0F, (byte) 0xFE, 0x05, 0x04, 0x0a}) //设置心跳数据，可以是String类型，也可以是byte[]
//                .setHeartBeatData("I'm is HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
                .build();

        mNettyTcpClient.setListener(MainActivity.this); //设置TCP监听
    }

    private void initView() {
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSendList.setLayoutManager(manager1);
        mSendList.setAdapter(mSendLogAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReceList.setLayoutManager(manager2);
        mReceList.setAdapter(mReceLogAdapter);

    }

    private void findViews() {
        mSendList = findViewById(R.id.send_list);
        mReceList = findViewById(R.id.rece_list);
        mSendET = findViewById(R.id.send_et);
        mConnect = findViewById(R.id.connect);
        mSendBtn = findViewById(R.id.send_btn);
        mClearLog = findViewById(R.id.clear_log);

        mConnect.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.connect:
                connect();
                break;

            case R.id.send_btn:
                if (!mNettyTcpClient.getConnectStatus()) {
                    Toast.makeText(getApplicationContext(), "未连接,请先连接", Toast.LENGTH_SHORT).show();
                } else {
//                    final String msg = mSendET.getText().toString();
//                    if (TextUtils.isEmpty(msg.trim())) {
//                        return;
//                    }
                    FollowersPlus.PBMessage.Builder builder = FollowersPlus.PBMessage.newBuilder();

                    FollowersPlus.PBMessageID.Builder pbmsgbd = FollowersPlus.PBMessageID.newBuilder();
                    pbmsgbd.setApiType(FollowersPlus.PBAPIType.FetchAppParams);
                    pbmsgbd.setDialogUUID(UUID.randomUUID().toString());
                    builder.setMessageID(pbmsgbd);

                    FollowersPlus.PBIdentity.Builder identity = FollowersPlus.PBIdentity.newBuilder();
                    identity.setSocialPlatformID(FollowersPlus.PBSocialPlatformType.Instagram);
                    identity.setAppID(FollowersPlus.PBAppType.FollowersPlus);
                    builder.setIdentity(identity);

                    FollowersPlus.PBFetchAppParamsRequest.Builder requestBuilder = FollowersPlus.PBFetchAppParamsRequest.newBuilder();
                    requestBuilder.setAppVersion("3.2.7");
                    builder.setPayload(requestBuilder.build().toByteString());

                    mNettyTcpClient.sendMsgToServer(builder.build(), new MessageStateListener() {
                        @Override
                        public void isSendSuccss(boolean isSuccess) {
                            if (isSuccess) {
                                Log.d(TAG, "Write auth successful");
//                                logSend(msg);
                            } else {
                                Log.d(TAG, "Write auth error");
                            }
                        }
                    });
                    mSendET.setText("");
                }

                break;

            case R.id.clear_log:
                mReceLogAdapter.getDataList().clear();
                mSendLogAdapter.getDataList().clear();
                mReceLogAdapter.notifyDataSetChanged();
                mSendLogAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void connect() {
        Log.d(TAG, "connect");
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//连接服务器
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    @Override
    public void onMessageResponseClient(FollowersPlus.PBMessage msg, int index) {
        try {
            byte[] inByte = msg.getPayload().toByteArray();
            // 字节转成对象
            FollowersPlus.PBFetchAppParamsReply pbFetchAppParamsReply = FollowersPlus.PBFetchAppParamsReply.parseFrom(inByte);
            logRece(index + ":" + pbFetchAppParamsReply.getParamsCount());
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    @Override
    public void onClientStatusConnectChanged(final int statusCode, final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    mConnect.setText("DisConnect:" + index);
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    mConnect.setText("Connect:" + index);
                }
            }
        });

    }

    private void logSend(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mSendLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendLogAdapter.notifyDataSetChanged();
            }
        });

    }

    private void logRece(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mReceLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceLogAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes, int length) {
        StringBuilder buf = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {// 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(bytes[i] & 0xFF)));
        }
        return buf.toString();
    }

    public void disconnect(View view) {
        mNettyTcpClient.disconnect();
    }
}
