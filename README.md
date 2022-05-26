	
欢迎使用红杉通Android视频会议SDK

前言
本文主要讲述如何在Android手机平台中使用给定的API接口，来连接、登录、新建和加入红杉通视频会议系统。
本文读者主要是给那些需要在Android APP中集成红杉通视频会议功能的开发者。

编译环境
Android Studio Arctic Fox | 2021.3.1 Patch 3
Android SDK版本: 29.0.2
Android Gradle插件版本: 4.1.3
Gradle版本: 6.8.3

主要接口（ConfAPI类）函数说明

1.ConfAPI ConfAPI.getInstance()；

功能描述
构建视频会议SDK API全局实例并且初始化所有全局会议变量。

输入参数说明
无

返回值说明
返回ConfAPI 全局对象。

调用示例
ConfAPI myConference = ConfAPI.getInstance();

2.boolean initSite(String url, Context context)

功能描述
初始化视频会议服务器地址信息。

输入参数说明
Url: 会议服务器地址。
Context: Activity上下文信息，用来保存配置信息、获取设备标识号和启动会议界面。

返回值说明
true: 初始化成功。
false: 初始化失败。

调用示例
//初始化服务器地址
if (!ConfAPI.getInstance().initSite(“http://192.168.2.161:80”, MainActivity.this)) {
    Log.d("InfowareLab.Debug", "Failed to initialize the server site");
    return;
}

3.LoginBean login(String userName, String password)

功能描述
登录视频会议服务器。

输入参数说明
userName： 登录用户名。
password：登录密码。

返回值说明
如果登录成功，返回登录信息LoginBean, 否则返回null。

调用示例
if (null == ConfAPI.getInstance().login(“admin”, “123456”) {
    Log.d("InfowareLab.Debug", "Failed to login the server");
    return;
}

4.String createConf(String confName, String confPwd, int duration)

功能描述
新建、启动和以主持人身份加入视频会议。

输入参数说明
String confName： 会议名称
String confPwd：会议密码
int duration: 会议时长，单位是分钟

返回值说明
如果创建会议成功，返回会议标识号，否则返回-1。

调用示例
String newConfId = ConfAPI.getInstance().createConf(confTopic, confPwd, 180);

5.void joinConf(int userId, String confId, String confPwd, String joinName)

功能描述
加入视频会议，根据userId自动判断是普通参加者还是主持人身份。

输入参数说明
String userId: 参加者用户标识号，由登录成功之后获得。如不需登录，则设置为0。
String confId： 会议号。
String confPwd：会议密码。
String joinName: 参加者名字。

返回值说明
无。执行结果会以handler方式通知，详见setNotifyHandler接口。

调用示例
ConfAPI.getInstance().joinConf(0, “62223781”, “”, “Frank”);

6.void setNotifyHandler(Handler notify)

功能描述
获取会议创建和加入过程中发生的错误通知。

输入参数说明
Handler notify: 接收通知的处理器

返回通知消息和参数定义
成功消息：WM_SUCCESS
错误消息：WM_ERROR
错误参数定义：
ERROR_GET_SITENAME: 无法获取站点名称
NEED_LOGIN: 该会议需要先登录
NO_CONFERENCE: 会议不存在
MEETINGNOTJOINBEFORE: 会议未开始
HOSTERROR: 无法连接到服务器
SPELLERROR: 用户名和密码格式错误
LOGINFAILED: 登录失败
CREATECONF_ERROR: 创建会议失败
MEETINGINVALIDATE: 会议不存在或已结束
INIT_SDK_FAILED: 初始化失败
CONF_CONFLICT: 会议冲突
IDENTITY_FAILED: 身份验证失败
BEYOUND_MAXNUM: 超出会议人数上限
CONF_OVER: 会议已结束
NOT_HOST: 没有管理员权限
BYOUND_STARTTIME: 开始时间早于系统当前时间
CHECK_SITE: 服务器站点格式错误
UNKNOWN_ERROR: 其他未知错误

调用示例
private Handler notifyHandler = new Handler() {
	@Override
	public void dispatchMessage(Message msg) {
		switch (msg.what) {
			case ConfAPI.WM_SUCCESS:
				showToast("加会成功。");
				break;
			case ConfAPI.WM_ERROR: 
				showToast("加会失败。错误码：" + msg.arg1);
				break;
		}
	}
};
ConfAPI.getInstance().setNotifyHandler(notifyHandler);




