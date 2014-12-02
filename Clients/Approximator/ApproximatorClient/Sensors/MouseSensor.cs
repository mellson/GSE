using System;
using System.Windows.Forms;
using Gma.UserActivityMonitor;

namespace ApproximatorClient.Sensors
{
    public class MouseSensor : AbstractSensor
    {
        public MouseSensor(string username, string name = "MouseSensor")
        {
            SensorName = name;
            UserName = username;
            HookManager.MouseMove += MouseMoved;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            StartUploading();
        }

        private void MouseMoved(object sender, MouseEventArgs e)
        {
            UpdateReadingForSensorName(SensorName, UserName, "Mouse Moved");
        }
    }
}
