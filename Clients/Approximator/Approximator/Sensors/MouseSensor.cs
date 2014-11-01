using System;
using System.Reactive.Linq;
using System.Windows.Forms;
using Gma.UserActivityMonitor;
using Newtonsoft.Json;

namespace Approximator.Sensors
{
    public class MouseSensor : AbstractSensor
    {
        public MouseSensor()
        {
            HookManager.MouseMove += MouseMoved;
            UpdateReadingForSensorName("MouseSensor");
            StartUploadingEverySecond();
        }

        private void MouseMoved(object sender, MouseEventArgs e)
        {
            UpdateReadingForSensorName("MouseSensor");
        }

        public void StartUploadingEverySecond()
        {
            var updateEverySecond = Observable.Interval(TimeSpan.FromSeconds(1));
            updateEverySecond.Subscribe(UploadHelper);
        }

        private void UploadHelper(long number)
        {
            UploadToServer(JsonConvert.SerializeObject(LastReading));
        }
    }
}
