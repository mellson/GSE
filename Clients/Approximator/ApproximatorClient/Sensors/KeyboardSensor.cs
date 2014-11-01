using System.Globalization;
using System.Windows.Forms;
using Gma.UserActivityMonitor;

namespace ApproximatorClient.Sensors
{
    class KeyboardSensor : AbstractSensor
    {
        public KeyboardSensor(string name = "KeyboardSensor")
        {
            SensorName = name;
            HookManager.KeyDown += HookManagerOnKeyDown;
            UpdateReadingForSensorName(SensorName, "Init");
            StartUploadingEverySecond();
        }

        private void HookManagerOnKeyDown(object sender, KeyEventArgs keyEventArgs)
        {
            UpdateReadingForSensorName(SensorName, keyEventArgs.KeyValue.ToString(CultureInfo.InvariantCulture));
        }
    }
}
