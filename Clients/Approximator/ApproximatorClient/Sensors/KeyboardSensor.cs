using System.Globalization;
using System.Windows.Forms;
using Gma.UserActivityMonitor;
using WebSocketSharp;

namespace ApproximatorClient.Sensors
{
    class KeyboardSensor : AbstractSensor
    {
        public KeyboardSensor(string username, WebSocket webSocket, string name = "KeyboardSensor")
        {
            SensorName = name;
            UserName = username;
            WebSocket = webSocket;
            HookManager.KeyDown += HookManagerOnKeyDown;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            StartUploading();
        }

        private void HookManagerOnKeyDown(object sender, KeyEventArgs keyEventArgs)
        {
            UpdateReadingForSensorName(SensorName, UserName, keyEventArgs.KeyValue.ToString(CultureInfo.InvariantCulture));
        }
    }
}
