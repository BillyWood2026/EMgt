' CCMS CultureMgt 8160 隐藏启动器：脱离调用方控制台/作业对象，避免 Shell 会话结束时收到 CTRL 事件被杀
Option Explicit
Dim sh, cmd
Set sh = CreateObject("WScript.Shell")
sh.CurrentDirectory = "D:\EMgtv3\CultureMgt"
cmd = "cmd /c java -Xmx384m -Dlogging.file.name=logs\culture8160.log -jar target\CultureMgt-1.0.0.jar > logs\culture-console.log 2>&1"
sh.Run cmd, 0, False
