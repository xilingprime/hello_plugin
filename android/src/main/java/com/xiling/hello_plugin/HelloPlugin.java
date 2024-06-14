package com.xiling.hello_plugin;


import android.content.Context;

import com.xiling.hello_plugin.bean.ReturnMessageBean;

import org.linphone.core.Account;
import org.linphone.core.AccountParams;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * HelloPlugin
 */
public class HelloPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private String TAG = "xiLingLinphone";

    private Core core;

    private EventChannel.EventSink callSink;

    private EventChannel.EventSink loginSink;

    private String sipInfoName = "";


    private Context applicationContext;

    private CoreListener coreListener = new CoreListenerStub() {
        @Override
        public void onAccountRegistrationStateChanged(Core core, Account account, RegistrationState state, String message) {
            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                loginSink.success("Event ERROR");
            } else if (state == RegistrationState.Ok) {
                loginSink.success(sipInfoName + "Event OK");
            }
        }

        @Override
        public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
            super.onCallStateChanged(core, call, state, message);
            if (state == Call.State.IncomingReceived) {
                Address remoteAddress = call.getRemoteAddress();
                String remoteSipAddress = remoteAddress.asStringUriOnly();
                ReturnMessageBean incomingReceived = new ReturnMessageBean("1", remoteSipAddress, message);
                callSink.success(incomingReceived.toJson());
            } else if (state == Call.State.OutgoingRinging) {
                ReturnMessageBean incomingReceived = new ReturnMessageBean("2", "对方振铃中", message);
                callSink.success(incomingReceived.toJson());
            } else if (state == Call.State.Connected) {
                ReturnMessageBean incomingReceived = new ReturnMessageBean("3", "连接了", message);
                callSink.success(incomingReceived.toJson());
            } else if (state == Call.State.Paused) {
                System.out.println("暂停");
            } else if (state == Call.State.End) {
                ReturnMessageBean incomingReceived = new ReturnMessageBean("4", "通话结束了", message);
                callSink.success(incomingReceived.toJson());
                System.out.println("消息为" + message);
                System.out.println("End");
            } else if (state == Call.State.Error) {
                System.out.println("Error");
            } else if (state == Call.State.Released) {
                System.out.println("Released");
                call.terminate();
            }
        }


    };

    @Override
    public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "hello_plugin");
        channel.setMethodCallHandler(this);
        applicationContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("init")) {
            init();
            result.success("linphone init success");
        } else if (call.method.equals("login")) {
            Map<String, String> arguments = (Map<String, String>) call.arguments;
            String userName = arguments.get("userName");
            String domain = arguments.get("domain");
            String password = arguments.get("password");
            String type = arguments.get("type");
            android.util.Log.i(TAG, "登录sip信息有 userName:" + userName + " \npassword:" + password + "\ndomain:" + domain + "\ntype:" + type);

//            login(userName, domain, password, type);
            result.success("login Ok");
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private void init() {
        android.util.Log.i(TAG, "init===============srart");
        Factory factory = Factory.instance();
        factory.setLoggerDomain("xiLingPrime init");
        this.core = factory.createCore(null, null, applicationContext);
        android.util.Log.i(TAG, "init===============success");
    }

    private String login(String userName, String domain, String password, String type) {
        android.util.Log.i(TAG, "login===============start");
        Address identity;
        //sip账号登录需要有两个对象，一个是的account对象，如何连接到代理服务器的，另一个是本地凭据对象，authinfo
        android.util.Log.i(TAG, "loginSipInfo userName:" + userName + " \npassword:" + password + "\ndomain:" + domain);
        //身份验证信息从Factory类中创建，因为这是个数据类
        AuthInfo authInfo = Factory.instance().createAuthInfo(userName, null, password, null, null, domain, null);
        //设置账号参数对象
        AccountParams accountParams = core.createAccountParams();
        //sip账号由身份地址标识，我们可以从用户名跟域中构建该地址，底层会调用c代码，如果创建失败，错误的使用会爆出引用未分配的存储地址
        try {
            identity = Factory.instance().createAddress("sip:" + userName + "@" + domain);
        } catch (Exception e) {
            android.util.Log.e(TAG, "构建身份标识地址异常，请提供正确的登录信息");
            return "构建身份标识地址异常，请提供正确的登录信息";
        }
        //设置sip的身份地址
        accountParams.setIdentityAddress(identity);
        //需要配置代理服务器位置
        Address address = Factory.instance().createAddress("sip:" + domain);
        //voip推荐使用两种，一种是udp一种是tls,
        if (address != null) {
            switch (type) {
                case "0":
                    address.setTransport(TransportType.Tcp);
                    break;
                case "1":
                    address.setTransport(TransportType.Udp);
                    break;
                case "2":
                    address.setTransport(TransportType.Tls);
                    break;
            }
        }
        //使用adree对象，进行传输协议，即为本地的地址为address，即是将sip信息发送到本地代理中，随后传到服务器中
        accountParams.setServerAddress(address);
        //表示是否启用注册
        accountParams.setRegisterEnabled(true);
        //将账号参数放进account对象，后面使用account对象登录信息
        Account account = core.createAccount(accountParams);
        //将两个对象分别放进core核心对象中
        core.addAuthInfo(authInfo);
        core.addAccount(account);
        //将本地默认账号设置为account对象中的信息
        core.setDefaultAccount(account);
        //添加核心对象监听器，监听各类信息，例如登录状态等
        core.addListener(coreListener);
        //向账号添加监听器，该监听器监听的是账号的状态
        account.addListener((account1, state, message) -> {
            //linphonesdK中默认带有日志的方法及其api
            Log.i("xiLingLinphone state changed: " + state + ", " + message);
        });
//        core.getConfig().setString("sound", "local_ring", "");
//        core.getConfig().setString("sound", "remote_ring", "");
        // 配置 Linphone, 例如设置 Echo Canceller
        //回音消除
        core.getConfig().setBool("audio", "echo canceller enabled", true);
        sipInfoName = userName;
        //启动核心类
        core.start();
        android.util.Log.i(TAG, "login===============success");
        return "启动了";
    }
}
