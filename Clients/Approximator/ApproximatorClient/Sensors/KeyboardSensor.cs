using System.Globalization;
using System.Windows.Forms;
using Gma.UserActivityMonitor;

namespace ApproximatorClient.Sensors
{
    class KeyboardSensor : AbstractSensor
    {
        public KeyboardSensor(string username, string name = "KeyboardSensor")
        {
            SensorName = name;
            UserName = username;
            HookManager.KeyDown += HookManagerOnKeyDown;
            UpdateReadingForSensorName(SensorName, UserName, "Init");
            StartUploading();
        }

        private void HookManagerOnKeyDown(object sender, KeyEventArgs keyEventArgs)
        {
            UpdateReadingForSensorName(SensorName, UserName, "Key Pressed");
        }
    }
}
