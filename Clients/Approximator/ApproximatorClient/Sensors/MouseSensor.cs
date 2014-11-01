using System;
using System.Windows.Forms;
using Gma.UserActivityMonitor;

namespace ApproximatorClient.Sensors
{
    public class MouseSensor : AbstractSensor
    {
        public MouseSensor(string name = "MouseSensor")
        {
            SensorName = name;
            HookManager.MouseMove += MouseMoved;
            UpdateReadingForSensorName(SensorName, "Init");
            StartUploading();
        }

        private void MouseMoved(object sender, MouseEventArgs e)
        {
            UpdateReadingForSensorName(SensorName, String.Format("X:{0},Y:{1}",e.X,e.Y));
        }
    }
}
