import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hello_plugin/enum/XiLingLinphoneTypeEnum.dart';
import 'package:hello_plugin/hello_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _helloPlugin = HelloPlugin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _helloPlugin.init() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            ElevatedButton(
                onPressed: () async {
                  await _helloPlugin.init();
                },
                child: const Text("初始化init")),
            ElevatedButton(
                onPressed: () async {
                  String loginInfo = await _helloPlugin.login(
                    domain: "1.14.33.160",
                    userName: "1012",
                    password: "123456",
                    typeEnum: XiLingLinphoneTypeEnum.UDP,
                  );
                  debugPrint("登录信息有$loginInfo");
                },
                child: const Text("登录")),
          ],
        ),
      ),
    );
  }
}
