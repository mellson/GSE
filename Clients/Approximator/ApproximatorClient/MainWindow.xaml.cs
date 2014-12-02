using System;
using System.Media;
using System.Threading;
using System.Windows;
using System.Windows.Media;
using ApproximatorClient.Sensors;
using log4net;
using WebSocketSharp;

namespace ApproximatorClient
{
    public partial class MainWindow
    {
        private static readonly ILog PromptLog = LogManager.GetLogger("PromptLogger");
        private static readonly ILog DataLog = LogManager.GetLogger("DataLogger");
        private readonly WebSocket _ws;
        private readonly string _userName;

        public MainWindow()
        {
            InitializeComponent();
            _ws = new WebSocket("ws://spcl.cloudapp.net:6696/users");
            _ws.OnMessage += (sender, e) => DataLog.Info(@"Response from WebSocket" + e.Data);
            _ws.Connect();
            _userName = Environment.UserName.Replace(" ","");
            _ws.Send("Connected .NET Client for user " + _userName);
            new MouseSensor(_userName, _ws);
            new KeyboardSensor(_userName, _ws);
            new FaceDetectionSensor(_userName, _ws, cameraIndex: 0);
            var visualBellThread = new Thread(VisualBell) { IsBackground = true };
            visualBellThread.Start();
        }

        public void VisualBell()
        {
            var sound = new SoundPlayer(@"Resources\Notify.wav");
            var random = new Random();
            while (true)
            {
                Dispatcher.Invoke(new Action(() => Top = 0));
                Dispatcher.Invoke(new Action(() => Left = SystemParameters.PrimaryScreenWidth - Width));
                Dispatcher.Invoke(new Action(() => Topmost = true));
                for (var i = 0; i <= 20; i++)
                {
                    Dispatcher.Invoke(i%2 == 0
                        ? (() => indicator.Fill = new SolidColorBrush(Colors.Black))
                        : new Action(() => indicator.Fill = new SolidColorBrush(Colors.White)));
                    if (i%10 == 0)
                        sound.Play();
                    Thread.Sleep(200);
                }
                Dispatcher.Invoke(new Action(() => Topmost = false));
                PromptLog.Info("Prompted user: " + _userName);
                _ws.Send("Prompted user: " + _userName);
                const int minutes20 = 60000*20;
                const int minutes40 = 60000*40;
                Thread.Sleep(random.Next(minutes20, minutes40)); // sleep for a random interval between 20-40 minutes
            }
        }
    }
}
