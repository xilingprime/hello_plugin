import 'package:flutter/services.dart';
import 'package:hello_plugin/enum/XiLingLinphoneTypeEnum.dart';

class HelloPlugin {
  final methodChannel = const MethodChannel('hello_plugin');

  Future<String?> init() async {
    var result = await methodChannel.invokeMethod<String>('init');
    return result;
  }

  Future<String> login({
    required String domain,
    required String userName,
    required String password,
    required XiLingLinphoneTypeEnum typeEnum,
  }) async {
    String result = await methodChannel.invokeMethod(
      'login',
      {
        "userName": userName,
        "domain": domain,
        "password": password,
        "type": typeEnum.index.toString(),
      },
    );
    return result;
  }
}
