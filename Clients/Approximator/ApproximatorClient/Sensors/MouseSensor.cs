using System;
using System.Windows.Forms;
using Gma.UserActivityMonitor;
using WebSocketSharp;

namespace ApproximatorClient.Sensors
{
    public class MouseSensor : AbstractSensor
    {
        public MouseSensor(string username, WebSocket webSocket, string name = "MouseSensor")
        {
            SensorName = name;
            UserName = username;
            WebSocket = webSocket;
            HookManager.MouseMove += MouseMoved;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            StartUploading();
        }

        private void MouseMoved(object sender, MouseEventArgs e)
        {
            UpdateReadingForSensorName(SensorName, UserName, String.Format("X:{0},Y:{1}", e.X, e.Y));
        }
    }
}
